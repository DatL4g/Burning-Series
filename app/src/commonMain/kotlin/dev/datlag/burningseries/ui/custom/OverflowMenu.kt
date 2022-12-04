package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import dev.datlag.burningseries.LocalStringRes

@Composable
fun OverflowMenu(
    tint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = {
        showMenu = !showMenu
    }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = LocalStringRes.current.more,
            tint = tint
        )
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        content()
    }
}