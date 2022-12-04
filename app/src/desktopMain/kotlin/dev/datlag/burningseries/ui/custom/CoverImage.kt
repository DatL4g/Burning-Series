package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import dev.datlag.burningseries.common.safeDecodeBase64
import dev.datlag.burningseries.model.Cover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.net.URL

@Composable
actual fun CoverImage(
    cover: Cover,
    description: String?,
    scale: ContentScale,
    modifier: Modifier,
    shape: Shape?
) {
    val image: ImageBitmap? by produceState<ImageBitmap?>(null) {
        value = withContext(Dispatchers.IO) {
            try {
                loadImageBitmap(cover.href)
            } catch (ignored: Throwable) {
                null
            }
        }
    }
    val base64Bytes = remember { cover.base64.safeDecodeBase64() }
    val base64Image = base64Bytes?.let { remember { Image.makeFromEncoded(it).toComposeImageBitmap() } }

    val displayImage = image ?: base64Image
    if (displayImage != null) {
        Image(
            bitmap = displayImage,
            contentDescription = description,
            contentScale = scale,
            modifier = if (shape != null) modifier.then(Modifier.clip(shape)) else modifier
        )
    }
}

internal fun loadImageBitmap(url: String): ImageBitmap = URL(url).openStream().buffered().use(::loadImageBitmap)
