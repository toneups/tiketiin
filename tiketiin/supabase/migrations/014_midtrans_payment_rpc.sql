
-- =====================================================
-- TIKETIIN DATABASE
-- Tahap 014: Midtrans Payment RPC
-- =====================================================

BEGIN;

CREATE OR REPLACE FUNCTION public.complete_midtrans_payment(
    p_order_id INTEGER,
    p_transaction_id TEXT,
    p_transaction_status TEXT,
    p_gross_amount NUMERIC,
    p_payload JSONB DEFAULT NULL
)
RETURNS TEXT
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO ''
AS $function$
DECLARE
    v_order public.orders%ROWTYPE;
    v_payment public.payments%ROWTYPE;
    v_qr_code TEXT;
    v_is_late BOOLEAN := FALSE;
BEGIN
    -- 1. Validasi input dari backend
    IF p_order_id IS NULL OR p_order_id <= 0 THEN
        RAISE EXCEPTION 'Order ID tidak valid';
    END IF;

    IF p_transaction_id IS NULL
       OR TRIM(p_transaction_id) = '' THEN
        RAISE EXCEPTION 'Transaction ID tidak valid';
    END IF;

    IF p_gross_amount IS NULL OR p_gross_amount <= 0 THEN
        RAISE EXCEPTION 'Nominal pembayaran tidak valid';
    END IF;

    -- Hanya status pembayaran sukses yang boleh diproses.
    -- Untuk capture, Edge Function wajib memverifikasi
    -- fraud_status = accept sebelum memanggil RPC ini.
    IF LOWER(TRIM(p_transaction_status))
       NOT IN ('settlement', 'capture') THEN
        RAISE EXCEPTION 'Status transaksi Midtrans belum berhasil';
    END IF;

    -- 2. Kunci order
    SELECT *
    INTO v_order
    FROM public.orders
    WHERE id = p_order_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Pesanan tidak ditemukan';
    END IF;

    -- 3. Kunci payment
    SELECT *
    INTO v_payment
    FROM public.payments
    WHERE order_id = p_order_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Data pembayaran tidak ditemukan';
    END IF;

    -- 4. Pastikan pembayaran berasal dari Midtrans
    IF UPPER(TRIM(v_payment.payment_method)) <> 'MIDTRANS' THEN
        RAISE EXCEPTION 'Metode pembayaran bukan Midtrans';
    END IF;

    -- 5. Validasi nominal dengan data database
    IF p_gross_amount <> v_payment.amount::NUMERIC THEN
        RAISE EXCEPTION 'Nominal pembayaran tidak sesuai';
    END IF;

    -- 6. Idempotensi: webhook yang sama tidak diproses ulang
    IF v_payment.status = 'SUCCESS'
       AND v_payment.payment_reference = p_transaction_id THEN
        RETURN 'ALREADY_PROCESSED';
    END IF;

    -- Jangan izinkan transaksi lain mengganti referensi
    -- pembayaran yang sudah sukses.
    IF v_payment.status = 'SUCCESS' THEN
        RAISE EXCEPTION 'Pembayaran sudah berhasil dengan transaksi lain';
    END IF;

    -- Referensi transaksi tidak boleh dipakai order lain.
    IF EXISTS (
        SELECT 1
        FROM public.payments
        WHERE payment_reference = p_transaction_id
          AND order_id <> p_order_id
    ) THEN
        RAISE EXCEPTION 'Transaction ID sudah digunakan';
    END IF;

    -- 7. Tentukan apakah pembayaran terlambat
    IF v_order.status IN ('EXPIRED', 'CANCELLED') THEN
        v_is_late := TRUE;

    ELSIF v_order.status = 'PENDING'
       AND v_order.expires_at <= clock_timestamp() THEN

        -- Jika scheduler belum memproses expiration,
        -- kembalikan stok dan ubah order menjadi EXPIRED.
        IF NOT public.restore_pending_order(
            p_order_id,
            'EXPIRED'
        ) THEN
            RAISE EXCEPTION 'Gagal memproses pesanan kedaluwarsa';
        END IF;

        v_is_late := TRUE;

    ELSIF v_order.status <> 'PENDING' THEN
        RAISE EXCEPTION 'Status pesanan tidak dapat diproses';
    END IF;

    -- 8. Pembayaran sukses setelah order expired/cancelled.
    -- Dana dicatat berhasil, tetapi escrow tetap PENDING.
    -- Order tidak diaktifkan kembali.
    IF v_is_late THEN

        IF v_payment.status NOT IN ('PENDING', 'EXPIRED', 'FAILED') THEN
            RAISE EXCEPTION 'Status pembayaran tidak dapat diproses';
        END IF;

        UPDATE public.payments
        SET
            payment_reference = p_transaction_id,
            status = 'SUCCESS',
            escrow_status = 'PENDING',
            paid_at = COALESCE(paid_at, NOW())
        WHERE order_id = p_order_id;

        INSERT INTO public.midtrans_payment_reviews (
            order_id,
            transaction_id,
            transaction_status,
            gross_amount,
            reason,
            review_status,
            payload
        )
        VALUES (
            p_order_id,
            p_transaction_id,
            LOWER(TRIM(p_transaction_status)),
            p_gross_amount,
            'PAYMENT_RECEIVED_AFTER_ORDER_EXPIRED_OR_CANCELLED',
            'OPEN',
            p_payload
        )
        ON CONFLICT (order_id)
        DO UPDATE SET
            transaction_id = EXCLUDED.transaction_id,
            transaction_status = EXCLUDED.transaction_status,
            gross_amount = EXCLUDED.gross_amount,
            reason = EXCLUDED.reason,
            review_status = 'OPEN',
            payload = EXCLUDED.payload;

        INSERT INTO public.notifications (
            user_id,
            title,
            message,
            type,
            event_id,
            order_id
        )
        VALUES (
            v_order.buyer_id,
            'Pembayaran Perlu Pemeriksaan',
            'Pembayaran Midtrans berhasil diterima setelah pesanan kedaluwarsa atau dibatalkan. Transaksi sedang menunggu pemeriksaan manual.',
            'PAYMENT',
            v_order.event_id,
            p_order_id
        );

        RETURN 'MANUAL_REVIEW';
    END IF;

    -- 9. Pembayaran normal: order masih PENDING
    IF v_payment.status <> 'PENDING' THEN
        RAISE EXCEPTION 'Status pembayaran tidak valid';
    END IF;

    -- Buat QR internal Tiketiin
    v_qr_code :=
        'TIKETIIN-' || pg_catalog.gen_random_uuid()::TEXT;

    -- 10. Tandai pembayaran sukses dan escrow ditahan
    UPDATE public.payments
    SET
        payment_reference = p_transaction_id,
        status = 'SUCCESS',
        escrow_status = 'HELD',
        paid_at = NOW()
    WHERE order_id = p_order_id
      AND status = 'PENDING';

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Gagal memperbarui status pembayaran';
    END IF;

    -- 11. Tandai order PAID dan simpan QR
    UPDATE public.orders
    SET
        status = 'PAID',
        qr_code = v_qr_code,
        paid_at = NOW()
    WHERE id = p_order_id
      AND status = 'PENDING';

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Gagal memperbarui status pesanan';
    END IF;

    -- 12. Perbarui status listing jika stok habis
    PERFORM 1
    FROM public.ticket_listings
    WHERE id = v_order.listing_id
    FOR UPDATE;

    UPDATE public.ticket_listings l
    SET status = 'TERJUAL'
    WHERE l.id = v_order.listing_id
      AND l.quantity = 0
      AND l.status = 'DIPESAN'
      AND NOT EXISTS (
          SELECT 1
          FROM public.orders o
          WHERE o.listing_id = l.id
            AND o.status = 'PENDING'
      );

    -- 13. Notifikasi buyer
    INSERT INTO public.notifications (
        user_id,
        title,
        message,
        type,
        event_id,
        order_id
    )
    VALUES (
        v_order.buyer_id,
        'Pembayaran Berhasil',
        'Pembayaran Midtrans berhasil. Dana ditahan dalam escrow.',
        'PAYMENT',
        v_order.event_id,
        p_order_id
    );

    -- 14. Notifikasi seller
    INSERT INTO public.notifications (
        user_id,
        title,
        message,
        type,
        event_id,
        order_id
    )
    VALUES (
        v_order.seller_id,
        'Tiket Berhasil Dibeli',
        'Buyer telah menyelesaikan pembayaran melalui Midtrans.',
        'ORDER',
        v_order.event_id,
        p_order_id
    );

    RETURN 'PAID';
END;
$function$;

-- 15. Hanya backend tepercaya yang boleh memanggil RPC.
REVOKE ALL ON FUNCTION
    public.complete_midtrans_payment(
        INTEGER, TEXT, TEXT, NUMERIC, JSONB
    )
FROM PUBLIC, anon, authenticated;

GRANT EXECUTE ON FUNCTION
    public.complete_midtrans_payment(
        INTEGER, TEXT, TEXT, NUMERIC, JSONB
    )
TO service_role;

COMMIT;
