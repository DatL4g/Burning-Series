package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun DropdownMenuItem(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
    contentPadding: PaddingValues,
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit
) {
    androidx.compose.material.DropdownMenuItem(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding
    ) {
        icon()
        Spacer(modifier = Modifier.padding(8.dp))
        text()
    }
}