package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import dev.datlag.burningseries.common.safeDecodeBase64
import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.other.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.awt.image.BufferedImage
import java.net.URL


@Composable
actual fun CoverImage(
    cover: Cover,
    description: String?,
    scale: ContentScale,
    fallbackIconTint: Color,
    modifier: Modifier,
    shape: Shape?
) {
    val image: ImageBitmap? by produceState<ImageBitmap?>(null) {
        value = withContext(Dispatchers.IO) {
            try {
                loadImageBitmap(Constants.getBurningSeriesUrl(cover.href))
            } catch (ignored: Throwable) {
                null
            }
        }
    }
    val base64Bytes = remember { cover.base64.safeDecodeBase64() }
    val base64Image = (base64Bytes ?: cover.base64.safeDecodeBase64())?.let { remember { Image.makeFromEncoded(it).toComposeImageBitmap() } }
    val displayImage = remember { image ?: base64Image }


    if (displayImage != null) {
        val trimmedImage = remember {
            if (shape != null) {
                displayImage.toAwtImage().trimImage()?.toComposeImageBitmap()
            } else {
                null
            } ?: displayImage
        }

        Image(
            bitmap = trimmedImage,
            contentDescription = description,
            contentScale = scale,
            modifier = if (shape != null) modifier.clip(shape) else modifier
        )
    } else {
        Image(
            imageVector = Icons.Default.NoPhotography,
            contentDescription = description,
            contentScale = ContentScale.Inside,
            modifier =  if (shape != null) modifier.clip(shape) else modifier,
            colorFilter = ColorFilter.tint(fallbackIconTint)
        )
    }
}

private fun BufferedImage.trimImage(): BufferedImage? {
    val raster = this.alphaRaster
    val width = raster.width
    val height = raster.height
    var left = 0
    var top = 0
    var right = width - 1
    var bottom = height - 1
    var minRight = width - 1
    var minBottom = height - 1
    top@ while (top <= bottom) {
        for (x in 0 until width) {
            if (raster.getSample(x, top, 0) != 0) {
                minRight = x
                minBottom = top
                break@top
            }
        }
        top++
    }
    left@ while (left < minRight) {
        for (y in height - 1 downTo top + 1) {
            if (raster.getSample(left, y, 0) != 0) {
                minBottom = y
                break@left
            }
        }
        left++
    }
    bottom@ while (bottom > minBottom) {
        for (x in width - 1 downTo left) {
            if (raster.getSample(x, bottom, 0) != 0) {
                minRight = x
                break@bottom
            }
        }
        bottom--
    }
    right@ while (right > minRight) {
        for (y in bottom downTo top) {
            if (raster.getSample(right, y, 0) != 0) {
                break@right
            }
        }
        right--
    }
    return this.getSubimage(left, top, right - left + 1, bottom - top + 1)
}

internal fun loadImageBitmap(url: String): ImageBitmap = URL(url).openStream().buffered().use(::loadImageBitmap)
