package dev.datlag.burningseries.ui.custom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import dev.datlag.burningseries.common.painter
import dev.datlag.burningseries.common.safeDecodeBase64
import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.other.Constants

@Composable
actual fun CoverImage(
    cover: Cover,
    description: String?,
    scale: ContentScale,
    fallbackIconTint: Color,
    modifier: Modifier,
    shape: Shape?
) {
    val base64Bytes = remember { cover.base64.safeDecodeBase64() }
    var imageScale: ContentScale = scale
    val base64Painter: Painter = (base64Bytes ?: cover.base64.safeDecodeBase64())?.let { rememberAsyncImagePainter(it) } ?: run {
        imageScale = ContentScale.Inside

        Icons.Default.NoPhotography.painter(fallbackIconTint, BlendMode.SrcIn)
    }

    AsyncImage(
        model = Constants.getBurningSeriesUrl(cover.href),
        contentDescription = description,
        error = remember { base64Painter },
        modifier = if (shape != null) modifier.then(Modifier.clip(shape)) else modifier,
        contentScale = imageScale
    )
}