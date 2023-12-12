package dev.datlag.burningseries.shared.ui.screen.initial.home.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import dev.datlag.burningseries.database.common.bestTitle
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.common.bounceClick
import dev.datlag.burningseries.shared.common.focusScale
import dev.datlag.burningseries.shared.common.onClick
import org.kodein.di.instance
import dev.datlag.burningseries.database.Series as DBSeries

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.SeriesItem(series: Home.Series, modifier: Modifier = Modifier, onClick: () -> Unit) {
    SeriesItem(
        title = series.bestTitle,
        coverHref = series.coverHref,
        modifier = modifier.animateItemPlacement(),
        onClick = onClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.SeriesItem(series: DBSeries, modifier: Modifier = Modifier, onClick: () -> Unit) {
    SeriesItem(
        title = series.bestTitle,
        coverHref = series.coverHref,
        modifier = modifier.animateItemPlacement(),
        onClick = onClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.SeriesItem(series: DBSeries, modifier: Modifier = Modifier, onClick: () -> Unit) {
    SeriesItem(
        title = series.bestTitle,
        coverHref = series.coverHref,
        modifier = modifier.animateItemPlacement(),
        onClick = onClick
    )
}

@Composable
private fun SeriesItem(title: String, coverHref: String?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedCard(
        modifier = modifier.focusScale(1.02F).height(150.dp).bounceClick().clip(MaterialTheme.shapes.medium).onClick {
            onClick()
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CardDefaults.elevatedShape
            ) {
                val platformContext: PlatformContext by LocalDI.current.instance()
                val imageLoader: ImageLoader by LocalDI.current.instance()

                AsyncImage(
                    model = ImageRequest.Builder(platformContext)
                        .data(coverHref?.let { BSUtil.getBurningSeriesLink(it) })
                        .placeholderMemoryCacheKey(coverHref)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = title,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .aspectRatio(1F, true)
                        .clip(CardDefaults.elevatedShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                )
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                    maxLines = 3
                )
            }
        }
    }
}