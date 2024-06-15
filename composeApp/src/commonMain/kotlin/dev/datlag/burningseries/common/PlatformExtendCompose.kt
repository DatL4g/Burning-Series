package dev.datlag.burningseries.common

import androidx.compose.ui.graphics.ImageBitmap
import com.vanniktech.blurhash.BlurHash

expect fun BlurHash.decode(
    hash: String?,
    width: Int,
    height: Int
): ImageBitmap?