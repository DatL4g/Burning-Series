package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.launchMain
import dev.datlag.burningseries.common.withIOContext
import kotlinx.coroutines.delay
import me.onebone.toolbar.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalToolbarApi::class)
@Composable
fun DefaultCollapsingToolbar(
    state: CollapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
    expandedBody: @Composable CollapsingToolbarScope.(CollapsingToolbarScaffoldState) -> Unit,
    title: @Composable (CollapsingToolbarScaffoldState) -> Unit,
    navigationIcon: @Composable (CollapsingToolbarScaffoldState) -> Unit = { },
    actions: @Composable RowScope.(CollapsingToolbarScaffoldState) -> Unit = { },
    content: @Composable CollapsingToolbarScaffoldScope.() -> Unit
) {
    val reversedProgress by remember {
        derivedStateOf { (abs(1F - state.toolbarState.progress)) }
    }
    var expanded by remember { mutableStateOf(false) }

    CollapsingToolbarScaffold(
        modifier = Modifier.fillMaxWidth(),
        state = state,
        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
        toolbarModifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp).verticalScroll(rememberScrollState()),
        toolbar = {
            expandedBody(state)

            TopAppBar(
                modifier = Modifier.pin(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = reversedProgress)
                ),
                title = {
                    title(state)
                },
                actions = {
                    actions(state)
                },
                navigationIcon = {
                    navigationIcon(state)
                }
            )
        }
    ) {
        content()
    }

    if (!expanded) {
        rememberCoroutineScope().launchMain {
            state.toolbarState.expand(0)
            repeat(5) {
                withIOContext {
                    delay(20)
                }
                state.toolbarState.expand(0)
            }
            expanded = true
        }
    }
}