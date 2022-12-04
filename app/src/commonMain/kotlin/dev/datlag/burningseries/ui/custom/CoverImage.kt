package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import dev.datlag.burningseries.model.Cover

@Composable
expect fun CoverImage(
    cover: Cover,
    description: String?,
    scale: ContentScale,
    modifier: Modifier = Modifier,
    shape: Shape? = null
)