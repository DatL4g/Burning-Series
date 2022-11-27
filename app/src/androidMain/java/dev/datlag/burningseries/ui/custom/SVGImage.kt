package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import java.io.InputStream

@Composable
actual fun SVGImage(
    stream: InputStream,
    description: String?,
    scale: ContentScale,
    tint: Color,
    modifier: Modifier
) {
    AsyncImage(
        model = stream.readBytes(),
        contentDescription = description,
        imageLoader = ImageLoader.Builder(LocalContext.current).components {
            add(SvgDecoder.Factory())
        }.build(),
        contentScale = scale,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier
    )
}