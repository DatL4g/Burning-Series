package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import dev.datlag.burningseries.network.model.Cover

@Composable
expect fun CoverImage(
    cover: Cover,
    description: String?,
    scale: ContentScale,
    modifier: Modifier = Modifier
)