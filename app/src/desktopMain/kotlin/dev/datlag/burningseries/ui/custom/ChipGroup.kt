package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
actual fun ChipGroup(
    modifier: Modifier,
    horizontalSpace: Dp,
    verticalAlignment: Alignment.Vertical,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(horizontalSpace),
        verticalAlignment = verticalAlignment
    ) {
        content()
    }
}