package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import dev.datlag.burningseries.common.safeDecodeBase64
import dev.datlag.burningseries.network.model.Cover

@Composable
actual fun CoverImage(
    cover: Cover,
    description: String?,
    scale: ContentScale,
    modifier: Modifier
) {
    val base64Bytes = remember { cover.base64.safeDecodeBase64() }
    val base64Painter = base64Bytes?.let { rememberAsyncImagePainter(it) }

    AsyncImage(
        model = cover.href,
        contentDescription = description,
        error = base64Painter,
        modifier = modifier,
        contentScale = scale
    )
}