package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.ui.navigation.screen.component.HomeCard

@Composable
fun SeriesSection(
    state: HomeState,
    modifier: Modifier = Modifier,
    onClick: (Home.Series) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Series",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            state is HomeState.Loading -> {
                Loading()
            }
            state.isSeriesError -> {
                Text(text = "Error loading series")
            }
            state is HomeState.Success -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    items(state.home.series.toList(), key = { it.href }) {
                        HomeCard(
                            series = it,
                            modifier = Modifier
                                .width(200.dp)
                                .height(280.dp),
                            onClick = onClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(fraction = 0.2F).clip(CircleShape)
        )
    }
}