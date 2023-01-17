package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun DialogSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit)