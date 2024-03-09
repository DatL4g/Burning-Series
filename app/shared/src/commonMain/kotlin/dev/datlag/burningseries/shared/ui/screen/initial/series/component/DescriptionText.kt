package dev.datlag.burningseries.shared.ui.screen.initial.series.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.onClick
import dev.datlag.burningseries.shared.ui.custom.ExpandableText
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun DescriptionText(description: String) {
    var expanded by remember { mutableStateOf(false) }

    ExpandableText(
        expanded = expanded,
        text = description,
        collapsedMaxLines = 2,
        modifier = Modifier.fillMaxWidth().animateContentSize().onClick {
            expanded = !expanded
        },
        toggle = {
            IconButton(
                onClick = {
                    expanded = !expanded
                }
            ) {
                val (icon, iconDescription) = if (expanded) {
                    Icons.Default.ExpandLess to stringResource(SharedRes.strings.read_less)
                } else {
                    Icons.Default.ExpandMore to stringResource(SharedRes.strings.read_more)
                }

                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}