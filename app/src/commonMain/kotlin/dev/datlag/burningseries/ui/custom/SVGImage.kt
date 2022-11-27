package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import java.io.InputStream

@Composable
expect fun SVGImage(
    stream: InputStream,
    description: String?,
    scale: ContentScale,
    tint: Color,
    modifier: Modifier = Modifier
)