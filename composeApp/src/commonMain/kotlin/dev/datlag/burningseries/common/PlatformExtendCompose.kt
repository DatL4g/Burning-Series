package dev.datlag.burningseries.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.vanniktech.blurhash.BlurHash

expect fun BlurHash.decode(
    hash: String?,
    width: Int,
    height: Int
): ImageBitmap?

@Composable
expect fun Modifier.drawProgress(color: Color, progress: Float): Modifier