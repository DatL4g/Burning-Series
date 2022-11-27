package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
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
            Tab(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clip(Shape.FullRoundedShape)
                    .background(if (selected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.tertiary),
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