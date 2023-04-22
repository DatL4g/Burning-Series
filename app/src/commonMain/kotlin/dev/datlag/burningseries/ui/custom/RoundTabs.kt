package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import dev.datlag.burningseries.LocalDarkMode
import dev.datlag.burningseries.common.ifFalse
import dev.datlag.burningseries.common.ifTrue
import dev.datlag.burningseries.ui.Shape

@Composable
fun RoundTabs(
    list: List<String>,
    selectedIndex: MutableValue<Int>
) {
    val selectedState = selectedIndex.subscribeAsState()

    TabRow(
        selectedTabIndex = selectedState.value,
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        modifier = Modifier.fillMaxWidth(),
        indicator = { _ ->
            Box {  }
        }
    ) {
        list.forEachIndexed { index, text ->
            val selected = selectedState.value == index
            val interactionSource = remember { MutableInteractionSource() }

            val isHovered by interactionSource.collectIsHoveredAsState()
            val isFocused by interactionSource.collectIsFocusedAsState()
            val applyBorder = isHovered || isFocused

            val borderColor = MaterialTheme.colorScheme.onTertiary

            Tab(
                modifier = Modifier
                    .padding(2.dp)
                    .ifTrue(applyBorder) {
                        border(2.dp, borderColor, Shape.FullRoundedShape)
                    }
                    .ifFalse(applyBorder) {
                        padding(2.dp)
                    }
                    .padding(4.dp)
                    .clip(Shape.FullRoundedShape)
                    .background(if (selected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.tertiary)
                    .hoverable(interactionSource).focusable(interactionSource = interactionSource),
                selected = selected,
                onClick = {
                    selectedIndex.value = index
                },
                text = {
                    Text(
                        text = text,
                        color = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onTertiary
                    )
                }
            )
        }
    }
}