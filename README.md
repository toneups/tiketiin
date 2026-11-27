# 🎟️ Tiketiin — Platform Jual Beli Tiket Event Secondhand

**Tiketiin** merupakan aplikasi marketplace tiket event secondhand berbasis Android yang menghubungkan penjual dan pembeli tiket dalam satu platform terintegrasi. Aplikasi ini bertujuan untuk menyediakan pengalaman transaksi tiket yang lebih aman, praktis, dan transparan melalui fitur verifikasi identitas, rekomendasi harga wajar, serta transaksi berbasis escrow.

Selain memfasilitasi jual beli tiket, Tiketiin juga membantu pengguna menemukan event sesuai minat dan memperoleh rekomendasi hotel di sekitar lokasi penyelenggaraan event.

---

## 📌 Latar Belakang

Tiket event populer sering kali terjual habis dalam waktu singkat. Kondisi tersebut mendorong sebagian orang untuk mencari tiket melalui pasar sekunder, seperti media sosial atau saluran transaksi informal.

Namun, transaksi melalui saluran tersebut memiliki sejumlah risiko, seperti penipuan tiket, harga jual yang tidak wajar, dan kurangnya perlindungan transaksi.

Tiketiin hadir sebagai solusi melalui platform terpusat yang memfasilitasi transaksi tiket secondhand dengan dukungan fitur keamanan dan transparansi harga.

## 🎯 Tujuan

- Menyediakan platform untuk jual beli tiket event secondhand.
- Meningkatkan keamanan transaksi melalui verifikasi identitas dan sistem escrow.
- Membantu pengguna mengetahui kewajaran harga jual tiket.
- Mempermudah pengguna menemukan event berdasarkan minat dan preferensi.
- Memberikan rekomendasi hotel di sekitar lokasi event.

---

## ✨ Fitur Utama

### 👤 Fitur Pembeli (Buyer)

| Fitur | Deskripsi |
|---|---|
| Registrasi dan Login | Memungkinkan pengguna membuat akun dan masuk ke aplikasi. |
| Beranda | Menampilkan berbagai informasi dan pilihan event yang tersedia. |
| Pencarian dan Filter | Mencari event berdasarkan kata kunci, kategori, lokasi, tanggal, dan harga. |
| For You | Memberikan rekomendasi event yang disesuaikan dengan minat dan aktivitas pengguna. |
| Wishlist | Menyimpan event yang diminati untuk dilihat kembali. |
| Detail Event | Menampilkan informasi event, lokasi, serta daftar tiket yang tersedia. |
| Checkout dan Pembayaran | Memfasilitasi proses pemesanan dan pembayaran tiket. |
| Manajemen Pesanan | Menampilkan riwayat transaksi dan tiket yang telah dibeli. |
| 360° Seat View | Memberikan gambaran perspektif tempat duduk terhadap panggung atau area event. |
| Rekomendasi Hotel Terdekat | Menampilkan rekomendasi penginapan di sekitar lokasi event setelah pembayaran berhasil. |

### 🏪 Fitur Penjual (Seller)

| Fitur | Deskripsi |
|---|---|
| Mode Jual (Sell Mode) | Memungkinkan pengguna beralih dari mode pembeli ke mode penjual menggunakan akun yang sama. |
| Verifikasi e-KYC | Memverifikasi identitas penjual sebelum melakukan penjualan tiket pertama. |
| Pembuatan Listing Tiket | Memungkinkan penjual menambahkan informasi tiket yang ingin dijual. |
| Rekomendasi Harga Wajar | Mengevaluasi harga jual tiket berdasarkan informasi harga dan data pasar. |
| Hot Opportunity | Menampilkan event dengan potensi permintaan tiket yang tinggi. |
| Manajemen Listing | Mengelola daftar tiket yang dipublikasikan dan aktivitas penjualan. |

### 🔐 Fitur Keamanan

- **Electronic Know Your Customer (e-KYC):** Mendukung proses verifikasi identitas penjual.
- **Escrow:** Mendukung perlindungan transaksi dengan menahan dana hingga persyaratan transaksi yang disepakati terpenuhi.
- **Rekomendasi Harga Wajar:** Membantu mengidentifikasi harga jual tiket yang berpotensi tidak wajar.

---

## 🛠️ Teknologi yang Digunakan

Berikut merupakan teknologi yang digunakan atau direncanakan dalam pengembangan aplikasi Tiketiin:

| Teknologi | Kegunaan |
|---|---|
| Kotlin | Bahasa pemrograman utama aplikasi Android. |
| Android Studio | Lingkungan pengembangan aplikasi Android. |
| Jetpack Compose | Pengembangan antarmuka pengguna secara deklaratif. |
| MVVM Architecture | Memisahkan tampilan, logika aplikasi, dan pengelolaan data. |
| Repository Pattern | Mengatur akses dan pengambilan data. |
| Kotlin Coroutines | Menangani proses asinkron. |
| StateFlow | Mengelola perubahan state secara reaktif. |
| Supabase | Layanan backend dan basis data PostgreSQL. |
| Supabase Authentication | Mengelola autentikasi pengguna. |
| Supabase Storage | Menyimpan file dan gambar. |
| Retrofit & OkHttp | Menangani komunikasi HTTP. |
| Room Database | Mengelola penyimpanan data lokal. |

---

## 🏗️ Arsitektur Aplikasi

Tiketiin menerapkan arsitektur **Model-View-ViewModel (MVVM)** yang dikombinasikan dengan Repository Pattern. Pendekatan ini digunakan untuk memisahkan tanggung jawab setiap komponen dan menjaga struktur kode agar lebih terorganisasi.

Alur umum pengelolaan data aplikasi:

```text
        Jetpack Compose UI
                |
                v
            ViewModel
                |
                v
             Use Case
                |
                v
            Repository
                |
          +-----+-----+
          |           |
          v           v
     Remote Data   Local Data
       Source       Source
          |           |
          v           v
       Supabase    Room Database
```

Arsitektur ini membantu meningkatkan keteraturan kode, mempermudah proses pengujian, serta memisahkan lapisan antarmuka dari pengelolaan data.

---

## 🔄 Alur Penggunaan Aplikasi

### Alur Pembeli (Buyer)

1. Pengguna melakukan registrasi atau login.
2. Pengguna menjelajahi berbagai event melalui halaman Beranda dan For You.
3. Pengguna memilih event dan melihat detail informasi event.
4. Pengguna memilih listing tiket yang tersedia.
5. Pengguna melanjutkan ke halaman checkout dan menyelesaikan pembayaran.
6. Sistem menampilkan konfirmasi transaksi.
7. Sistem memberikan rekomendasi hotel terdekat berdasarkan lokasi event.
8. Pengguna dapat melihat tiket yang dibeli dan riwayat transaksi.

### Alur Penjual (Seller)

1. Pengguna beralih ke Mode Jual (Sell Mode).
2. Pengguna melakukan verifikasi identitas melalui e-KYC apabila diperlukan untuk penjualan pertama.
3. Pengguna membuat listing tiket.
4. Pengguna memasukkan informasi tiket, harga asli, dan harga jual.
5. Sistem mengevaluasi harga melalui fitur Rekomendasi Harga Wajar.
6. Pengguna mempublikasikan listing tiket.
7. Pengguna mengelola listing dan aktivitas penjualan.

---

## 🏨 Rekomendasi Hotel Terdekat

Tiketiin menyediakan fitur Rekomendasi Hotel Terdekat untuk membantu pembeli menemukan penginapan di sekitar lokasi penyelenggaraan event.

Proses rekomendasi hotel secara umum meliputi:

1. Sistem mengambil data lokasi venue dan tanggal event.
2. Sistem memperoleh koordinat lokasi venue.
3. Sistem mengambil data hotel yang tersedia.
4. Sistem menghitung jarak antara venue dan hotel menggunakan rumus Haversine.
5. Sistem menyaring hotel dalam radius 5 km.
6. Jika tidak ditemukan hotel yang sesuai, pencarian diperluas hingga radius 10 km.
7. Sistem memeriksa ketersediaan kamar pada tanggal event.
8. Sistem mengurutkan rekomendasi berdasarkan jarak dan informasi pendukung lainnya.
9. Sistem menampilkan lima rekomendasi hotel terbaik.

Pengguna dapat memilih hotel untuk melanjutkan pemesanan melalui tautan pemesanan yang tersedia atau melewati rekomendasi dan kembali ke halaman pesanan.

---

## 📂 Struktur Proyek

Aplikasi Tiketiin menggunakan struktur direktori yang disesuaikan dengan kebutuhan pengembangan aplikasi Android.

```text
Tiketiin/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── ...
│           └── res/
├── gradle/
├── supabase/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── README.md
```

Struktur package di dalam aplikasi dapat disesuaikan dengan implementasi dan pembagian fitur selama proses pengembangan.

---

## 🚀 Cara Menjalankan Aplikasi

### Persyaratan

Sebelum menjalankan aplikasi, pastikan perangkat pengembangan telah memiliki:

- Android Studio.
- Android SDK yang telah dikonfigurasi.
- JDK yang sesuai dengan konfigurasi Gradle proyek.
- Git.
- Akses ke konfigurasi Supabase proyek.

### Langkah Instalasi

**1. Clone repository**

```bash
git clone <REPOSITORY_URL>
```

**2. Buka proyek**

Buka folder hasil clone melalui Android Studio.

**3. Konfigurasi backend**

Siapkan konfigurasi Supabase yang dibutuhkan, termasuk URL proyek dan API key yang sesuai dengan konfigurasi aplikasi.

Jangan mengunggah kredensial privat atau secret key ke repository publik.

**4. Sinkronisasi Gradle**

Tunggu hingga proses sinkronisasi Gradle dan pengunduhan dependensi selesai.

**5. Jalankan aplikasi**

Hubungkan perangkat Android atau jalankan emulator, kemudian klik tombol **Run** di Android Studio.

---

## 👥 Tim Pengembang

Tiketiin dikembangkan secara kolaboratif sebagai bagian dari proyek akademik.

| Anggota Tim | Tanggung Jawab |
|---|---|
| Qotratun Nada S. | Autentikasi, pencarian dan penjelajahan event, wishlist, pembelian, manajemen tiket, dan For You. |
| Naila Amelia | Pengembangan fitur Hot Opportunity. |
| Raisya Nadiva | Penyusunan bab pendahuluan dan pengembangan fitur Rekomendasi Harga Wajar. |
| Saskia Negita F. | Pengelolaan data hotel, data venue, dan pengembangan fitur Rekomendasi Hotel Terdekat. |

---

## 📌 Ruang Lingkup Proyek

Tiketiin berfokus pada pengembangan marketplace tiket event secondhand yang terintegrasi dengan fitur pendukung bagi pembeli dan penjual.

Ruang lingkup pengembangan mencakup pencarian event, pengelolaan listing tiket, alur transaksi, verifikasi penjual, rekomendasi harga, serta rekomendasi akomodasi di sekitar lokasi event.

Beberapa mekanisme transaksi dan integrasi layanan dapat bergantung pada layanan eksternal serta tahap implementasi proyek.

---

## 📄 Lisensi

Proyek ini dikembangkan untuk keperluan akademik. Hak dan kepemilikan kode sumber berada pada kontributor proyek sesuai kesepakatan tim.
