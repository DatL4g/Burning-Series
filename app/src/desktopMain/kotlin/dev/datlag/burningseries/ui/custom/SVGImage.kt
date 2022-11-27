package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import java.io.InputStream

@Composable
actual fun SVGImage(
    stream: InputStream,
    description: String?,
    scale: ContentScale,
    tint: Color,
    modifier: Modifier
) {
    val density = LocalDensity.current
    val svgPainter = remember { loadSvgPainter(stream, density) }

    Image(
        painter = svgPainter,
        contentDescription = description,
        modifier = modifier,
        contentScale = scale,
        colorFilter = ColorFilter.tint(tint)
    )
}