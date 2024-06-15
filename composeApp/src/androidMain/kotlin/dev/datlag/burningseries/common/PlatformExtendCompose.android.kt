package dev.datlag.burningseries.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.vanniktech.blurhash.BlurHash

actual fun BlurHash.decode(
    hash: String?,
    width: Int,
    height: Int
): ImageBitmap? {
    if (hash.isNullOrBlank()) {
        return null
    }

    val bitmap = decode(
        blurHash = hash,
        width = width,
        height = height
    )
    return bitmap?.asImageBitmap()
}