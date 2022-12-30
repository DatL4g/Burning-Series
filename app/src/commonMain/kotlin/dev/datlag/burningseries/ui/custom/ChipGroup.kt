package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
expect fun ChipGroup(
    modifier: Modifier = Modifier,
    horizontalSpace: Dp,
    verticalAlignment: Alignment.Vertical,
    content: @Composable () -> Unit
)