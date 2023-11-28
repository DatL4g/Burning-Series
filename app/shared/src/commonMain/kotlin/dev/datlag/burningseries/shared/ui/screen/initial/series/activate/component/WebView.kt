package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun WebView(url: String, modifier: Modifier = Modifier, onScraped: (String) -> Unit)