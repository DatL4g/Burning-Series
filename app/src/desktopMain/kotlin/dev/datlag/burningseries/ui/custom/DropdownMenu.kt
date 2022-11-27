package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

@Composable
actual fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material.DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        content()
    }
}