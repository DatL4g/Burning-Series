package dev.datlag.burningseries.ui.navigation.screen.medium.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.description
import dev.datlag.burningseries.ui.navigation.screen.medium.MediumComponent
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.typography
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max

@Composable
internal fun DescriptionSection(
    component: MediumComponent,
    modifier: Modifier = Modifier
) {
    val description by component.seriesDescription.collectAsStateWithLifecycle(null)

    if (!description.isNullOrBlank()) {
        Column(
            modifier = modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var descriptionExpandable by remember(description) { mutableStateOf(false) }
            var descriptionExpanded by remember(description) { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1F),
                    text = stringResource(Res.string.description),
                    style = Platform.typography().headlineSmall
                )
            }

            val animatedLines by animateIntAsState(
                targetValue = if (descriptionExpanded) {
                    Int.MAX_VALUE
                } else {
                    3
                },
                animationSpec = tween()
            )

            SelectionContainer {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = description!!,
                    maxLines = max(animatedLines, 1),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { result ->
                        if (!descriptionExpanded) {
                            descriptionExpandable = result.hasVisualOverflow
                        }
                    }
                )
            }
            if (descriptionExpandable) {
                IconButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        descriptionExpanded = !descriptionExpanded
                    }
                ) {
                    val icon = if (descriptionExpanded) {
                        Icons.Rounded.ExpandLess
                    } else {
                        Icons.Rounded.ExpandMore
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }
        }
    }
}