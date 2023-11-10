package dev.datlag.burningseries.ui.screen.initial.home.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.bounceClick
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.ui.theme.loadImageScheme
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.SeriesItem(series: Home.Series, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.animateItemPlacement().height(150.dp).bounceClick().onClick {
            onClick()
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CardDefaults.elevatedShape
            ) {
                series.coverHref?.let { cover ->
                    when (val resource = asyncPainterResource(BSUtil.getBurningSeriesLink(cover))) {
                        is Resource.Loading, is Resource.Failure -> {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1F, true)
                                    .clip(CardDefaults.elevatedShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                            )
                        }
                        is Resource.Success -> {
                            Image(
                                painter = resource.value,
                                contentScale = ContentScale.FillWidth,
                                contentDescription = series.title,
                                modifier = Modifier.aspectRatio(1F, true)
                            )
                            loadImageScheme(BSUtil.fixSeriesHref(series.href), resource.value)
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = series.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                    maxLines = 3
                )
            }
        }
    }
}