package dev.datlag.burningseries.shared.ui.custom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import dev.datlag.burningseries.shared.LocalDI
import org.kodein.di.instance

@Composable
fun Cover(
    key: String?,
    data: Any?,
    contentDescription: String?,
    contentScale: ContentScale = ContentScale.FillWidth,
    modifier: Modifier = Modifier,
    errorPainter: Painter = rememberVectorPainter(Icons.Default.NoPhotography),
    errorColorFilter: ColorFilter? = ColorFilter.tint(LocalContentColor.current),
    errorScale: ContentScale = ContentScale.Inside,
    onSuccess: (AsyncImagePainter.State.Success) -> Unit = {}
) {
    val platformContext: PlatformContext by LocalDI.current.instance()
    val imageLoader: ImageLoader by LocalDI.current.instance()
    var scale by remember { mutableStateOf<ContentScale>(contentScale) }
    var filter by remember { mutableStateOf<ColorFilter?>(null) }

    AsyncImage(
        model = ImageRequest.Builder(platformContext)
            .data(data)
            .placeholderMemoryCacheKey(key)
            .build(),
        error = errorPainter,
        imageLoader = imageLoader,
        contentDescription = contentDescription,
        contentScale = scale,
        modifier = modifier,
        onLoading = {
            scale = contentScale
            filter = null
        },
        onError = {
            scale = errorScale
            filter = errorColorFilter
        },
        onSuccess = {
            scale = contentScale
            filter = null
            onSuccess(it)
        },
        colorFilter = filter
    )
}