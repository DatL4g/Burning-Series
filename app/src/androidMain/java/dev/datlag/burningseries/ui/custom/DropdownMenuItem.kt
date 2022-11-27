package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun DropdownMenuItem(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
    contentPadding: PaddingValues,
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit
) {
    androidx.compose.material3.DropdownMenuItem(
        text = {
            text()
        },
        leadingIcon = {
            icon()
        },
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = contentPadding
    )
}