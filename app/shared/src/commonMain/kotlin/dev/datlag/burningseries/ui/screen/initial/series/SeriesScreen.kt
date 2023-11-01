package dev.datlag.burningseries.ui.screen.initial.series

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.ui.custom.DefaultCollapsingToolbar
import kotlin.math.abs

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SeriesScreen(component: SeriesComponent) {
    when (calculateWindowSizeClass().widthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactScreen(component)
        else -> DefaultScreen(component)
    }
}

@Composable
private fun CompactScreen(component: SeriesComponent) {
    DefaultCollapsingToolbar(
        expandedBody = { state ->
            Text(
                text = component.initialTitle,
                modifier = Modifier.road(Alignment.TopStart, Alignment.BottomStart).padding(16.dp),
                color = LocalContentColor.current.copy(alpha = run {
                    val alpha = state.toolbarState.progress
                    if (alpha < 0.7F) {
                        if (alpha < 0.3F) {
                            0F
                        } else {
                            alpha
                        }
                    } else {
                        1F
                    }
                }),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        title = { state ->
            val reversedProgress by remember {
                derivedStateOf { (abs(1F - state.toolbarState.progress)) }
            }
            Text(
                text = component.initialTitle,
                color = LocalContentColor.current.copy(alpha = run {
                    val alpha = reversedProgress
                    if (alpha < 0.7F) {
                        if (alpha < 0.3F) {
                            0F
                        } else {
                            alpha
                        }
                    } else {
                        1F
                    }
                }),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        navigationIcon = {

        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {

        }
    }
}

@Composable
private fun DefaultScreen(component: SeriesComponent) {

}