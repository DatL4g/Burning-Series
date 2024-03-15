package dev.datlag.burningseries.shared.ui.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.common.LocalPadding
import dev.datlag.burningseries.shared.common.launchMain
import dev.datlag.burningseries.shared.common.withIOContext
import dev.datlag.burningseries.shared.ui.custom.toolbar.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultCollapsingToolbar(
    state: CollapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
    forceExpand: Boolean = false,
    expandedBody: @Composable CollapsingToolbarScope.(CollapsingToolbarScaffoldState) -> Unit,
    title: @Composable (CollapsingToolbarScaffoldState) -> Unit,
    navigationIcon: (@Composable (CollapsingToolbarScaffoldState) -> Unit)? = null,
    actions: @Composable RowScope.(CollapsingToolbarScaffoldState) -> Unit = { },
    content: @Composable CollapsingToolbarScaffoldScope.() -> Unit
) {
    val reversedProgress by remember {
        derivedStateOf { (abs(1F - state.toolbarState.progress)) }
    }

    CollapsingToolbarScaffold(
        modifier = Modifier.fillMaxSize(),
        state = state,
        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
        toolbarModifier = Modifier.fillMaxWidth(),
        toolbarScrollable = true,
        toolbar = {
            expandedBody(state)

            TopAppBar(
                modifier = Modifier.fillMaxSize(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = reversedProgress),
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    navigationIcon?.invoke(state)
                },
                title = {
                    title(state)
                },
                actions = {
                    actions(state)
                }
            )
        }
    ) {
        content()
    }

    LaunchedEffect(forceExpand) {
        if (forceExpand) {
            state.toolbarState.expand(0)
        }
    }
}