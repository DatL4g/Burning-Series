package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun DropdownMenuItem(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit
)