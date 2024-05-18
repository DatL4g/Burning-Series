package dev.datlag.burningseries.shared.ui.screen.initial.series.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

/**
 * Keep single implementation instead of ProjectCard, to support hint for not connected.
 */
@Composable
expect fun AniFlowCard(
    isAnime: StateFlow<Boolean>,
    modifier: Modifier = Modifier
)