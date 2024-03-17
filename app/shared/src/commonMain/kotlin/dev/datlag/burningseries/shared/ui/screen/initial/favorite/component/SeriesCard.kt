package dev.datlag.burningseries.shared.ui.screen.initial.favorite.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.database.common.bestTitle
import dev.datlag.burningseries.database.common.mainTitle
import dev.datlag.burningseries.database.common.subTitle
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.shared.common.bottomShadowBrush
import dev.datlag.burningseries.shared.ui.custom.Cover
import dev.datlag.burningseries.shared.ui.theme.SchemeTheme
import dev.datlag.burningseries.shared.ui.theme.rememberSchemeThemeDominantColorState

@Composable
fun SeriesCard(
    series: Series,
    modifier: Modifier = Modifier,
    onClick: (Series) -> Unit
) {
    SchemeTheme(
        key = series.hrefPrimary
    ) {
        Card(
            modifier = modifier,
            onClick = {
                onClick(series)
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val scope = rememberCoroutineScope()
                val colorState = rememberSchemeThemeDominantColorState(
                    key = series.hrefPrimary,
                    applyMinContrast = true,
                    minContrastBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
                )
                val animatedColor by animateColorAsState(
                    targetValue = colorState.color
                )

                Cover(
                    key = series.coverHref,
                    data = series.coverHref?.let { BSUtil.getBurningSeriesLink(it) },
                    contentDescription = series.bestTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onSuccess = { state ->
                        SchemeTheme.update(
                            key = series.hrefPrimary,
                            input = state.painter,
                            scope = scope
                        )
                    }
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .bottomShadowBrush(animatedColor)
                        .padding(16.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        text = series.mainTitle,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        color = colorState.onColor
                    )
                    series.subTitle?.let {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth(),
                            color = colorState.onColor,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}