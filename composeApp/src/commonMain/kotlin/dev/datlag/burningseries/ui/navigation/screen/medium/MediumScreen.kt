package dev.datlag.burningseries.ui.navigation.screen.medium

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle

@Composable
fun MediumScreen(component: MediumComponent) {
    Column {
        val state by component.seriesState.collectAsStateWithLifecycle()

        Text(text = component.initialSeriesData.title)
        Text(text = state.toString())
    }
}