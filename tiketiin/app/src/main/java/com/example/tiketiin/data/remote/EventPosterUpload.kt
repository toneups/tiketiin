package com.example.tiketiin.data.remote

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

suspend fun uploadEventPoster(
    resolver: ContentResolver,
    uri: Uri,
    userId: String
): String = withContext(Dispatchers.IO) {
    val mimeType = resolver.getType(uri)
    val extension = when (mimeType) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> throw IllegalArgumentException("Pilih poster JPG, PNG, atau WebP.")
    }

    val maxBytes = 5 * 1024 * 1024
    val bytes = resolver.openInputStream(uri)?.use { input ->
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var count = input.read(buffer)

        while (count != -1) {
            require(output.size() + count <= maxBytes) {
                "Ukuran poster maksimal 5 MB."
            }
            output.write(buffer, 0, count)
            count = input.read(buffer)
        }

        output.toByteArray()
    } ?: throw IllegalArgumentException("Foto poster tidak bisa dibuka.")

    require(bytes.isNotEmpty()) { "File poster kosong." }

    val path = "$userId/${UUID.randomUUID()}.$extension"
    val body = bytes.toRequestBody(checkNotNull(mimeType).toMediaType())
    RetrofitClient.api.uploadEventPoster(path, body)

    "${SupabaseConfig.BASE_URL.trimEnd('/')}/storage/v1/object/public/event-posters/$path"
}