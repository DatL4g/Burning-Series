package dev.datlag.burningseries.ui.navigation.screen.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.datlag.burningseries.ui.custom.AndroidFixWindowSize
import dev.datlag.burningseries.ui.navigation.screen.home.component.CompactScreen
import dev.datlag.burningseries.ui.navigation.screen.home.component.FavoritesScreen
import dev.datlag.burningseries.ui.navigation.screen.home.component.HomeSearchBar
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    AndroidFixWindowSize {
        val dialogState by component.dialog.subscribeAsState()
        val release by component.release.collectAsStateWithLifecycle(null)
        val displayRelease by component.displayRelease.collectAsStateWithLifecycle()

        LaunchedEffect(release, displayRelease) {
            if (displayRelease) {
                release?.let(component::release)
            }
        }

        dialogState.child?.instance?.render()

        Scaffold(
            topBar = {
                HomeSearchBar(component)
            },
        ) { padding ->
            val state by component.home.collectAsStateWithLifecycle()

            CompactScreen(
                state = state,
                padding = padding,
                component = component
            )
        }
    }
}
