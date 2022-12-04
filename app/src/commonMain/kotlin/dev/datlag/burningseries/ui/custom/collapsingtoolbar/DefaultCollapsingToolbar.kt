package dev.datlag.burningseries.ui.custom.collapsingtoolbar

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import dev.datlag.burningseries.common.CommonDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import dev.datlag.burningseries.other.Logger

@Composable
fun DefaultCollapsingToolbar(
    state: CollapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
    expandedBody: @Composable CollapsingToolbarScope.(CollapsingToolbarScaffoldState) -> Unit,
    title: @Composable (CollapsingToolbarScaffoldState) -> Unit,
    navigationIcon: @Composable ((CollapsingToolbarScaffoldState) -> Unit)? = null,
    actions: @Composable ((CollapsingToolbarScaffoldState) -> Unit)? = null,
    content: @Composable CollapsingToolbarScaffoldScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val reversedProgress by remember {
        derivedStateOf { (abs(1F - state.toolbarState.progress)) }
    }
    var expanded by remember { mutableStateOf(false) }

    CollapsingToolbarScaffold(
        modifier = Modifier.fillMaxWidth(),
        state = state,
        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
        toolbarModifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
        toolbar = {
            expandedBody(state)

            TopAppBar(
                modifier = Modifier.pin(),
                backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = reversedProgress),
                elevation = 0.dp,
                navigationIcon = {
                    navigationIcon?.invoke(state)
                },
                title = {
                    title.invoke(state)
                },
                actions = {
                    actions?.invoke(state)
                }
            )
        }
    ) {
        content()
    }

    if (!expanded) {
        scope.launch(CommonDispatcher.Main) {
            state.toolbarState.expand(0)
            repeat(5) {
                withContext(Dispatchers.IO) {
                    delay(20)
                }
                state.toolbarState.expand(0)
            }
            expanded = true
        }
    }
}