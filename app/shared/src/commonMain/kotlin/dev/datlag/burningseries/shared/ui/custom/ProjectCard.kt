package dev.datlag.burningseries.shared.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.datlag.burningseries.shared.other.Project

@Composable
expect fun ProjectCard(project: Project, modifier: Modifier = Modifier)