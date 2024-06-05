package dev.datlag.burningseries.ui.navigation.screen.medium

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun MediumScreen(component: MediumComponent) {
    Text(text = component.initialSeriesData.title)
}