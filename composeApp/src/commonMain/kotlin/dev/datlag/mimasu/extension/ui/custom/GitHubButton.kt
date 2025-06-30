package dev.datlag.mimasu.extension.ui.custom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.datlag.mimasu.extension.LocalDarkMode
import dev.datlag.tooling.compose.platform.PlatformBorder
import dev.datlag.tooling.compose.platform.PlatformButton
import dev.datlag.tooling.compose.platform.PlatformButtonBorder
import dev.datlag.tooling.compose.platform.PlatformButtonColors
import dev.datlag.tooling.compose.platform.PlatformIcon
import dev.datlag.tooling.compose.platform.PlatformText

@Composable
fun GitHubButton(
    onClick: () -> Unit,
    darkContainer: Boolean = !LocalDarkMode.current,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSize: Dp = ButtonDefaults.IconSize
) {
    val containerColor = remember(darkContainer) {
        if (darkContainer) {
            Color(0xFF24292e)
        } else {
            Color(0xFF2b3137)
        }
    }
    val contentColor = remember(darkContainer) {
        if (darkContainer) {
            Color(0xFFfafbfc)
        } else {
            Color(0xFFFFFFFF)
        }
    }
    val border = remember(enabled, darkContainer) {
        if (!enabled || darkContainer) {
            null
        } else {
            BorderStroke(
                width = 1.dp,
                color = contentColor.copy(alpha = 0.5F)
            )
        }
    }

    PlatformButton(
        modifier = modifier,
        onClick = { onClick() },
        enabled = enabled,
        colors = PlatformButtonColors.default(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = PlatformButtonBorder.default(
            border = if (border == null) {
                PlatformBorder.None
            } else {
                PlatformBorder(
                    border = border
                )
            }
        )
    ) {
        PlatformIcon(
            modifier = Modifier.size(iconSize),
            imageVector = Symbols.Github,
            contentDescription = null
        )
        Spacer(modifier = Modifier.defaultMinSize(minWidth = ButtonDefaults.IconSpacing).weight(1F))
        PlatformText(
            text = text,
            maxLines = 1,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1F))
    }
}