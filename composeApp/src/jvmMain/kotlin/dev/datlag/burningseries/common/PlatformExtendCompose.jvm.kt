package dev.datlag.burningseries.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.vanniktech.blurhash.BlurHash

actual fun BlurHash.decode(
    hash: String?,
    width: Int,
    height: Int
): ImageBitmap? {
    if (hash.isNullOrBlank()) {
        return null
    }

    val image = decode(
        blurHash = hash,
        width = width,
        height = height
    )
    return image?.toComposeImageBitmap()
}

@Composable
actual fun Modifier.drawProgress(color: Color, progress: Float): Modifier = drawWithContent {
    with(drawContext.canvas.nativeCanvas) {
        val checkPoint = saveLayer(null, null)

        drawContent()

        drawRect(
            color = color,
            size = Size(size.width * progress, size.height),
            blendMode = BlendMode.SrcOut
        )

        restoreToCount(checkPoint)
    }
}