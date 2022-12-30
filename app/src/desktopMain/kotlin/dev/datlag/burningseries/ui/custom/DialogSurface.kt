package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.SemiBlack

@Composable
actual fun DialogSurface(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.SemiBlack,
        elevation = 0.dp
    ) {
        content()
    }
}