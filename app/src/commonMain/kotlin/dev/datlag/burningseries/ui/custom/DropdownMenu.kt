package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

@Composable
expect fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
)