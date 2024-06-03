package dev.datlag.burningseries.ui.navigation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.common.merge
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.ui.custom.AndroidFixWindowSize
import dev.datlag.burningseries.ui.navigation.screen.component.HidingNavigationBar
import dev.datlag.burningseries.ui.navigation.screen.component.HomeCard
import dev.datlag.burningseries.ui.navigation.screen.component.NavigationBarState
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    AndroidFixWindowSize {
        when (calculateWindowSizeClass().widthSizeClass) {
            WindowWidthSizeClass.Compact -> CompactScreen(component)
            else -> BigScreen(component)
        }
    }
}

@Composable
fun ContentView(paddingValues: PaddingValues, component: HomeComponent) {
    val homeState by component.home.collectAsStateWithLifecycle()

    when (val current = homeState) {
        is HomeState.Loading -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(0.2F).clip(CircleShape)
                )
            }
        }
        is HomeState.Failure -> {
            Text(text = "Error please try again")
        }
        is HomeState.Success -> {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.FixedSize(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = paddingValues.merge(16.dp)
            ) {
                items(current.home.series.toImmutableList(), key = { it.href }) {
                    HomeCard(
                        series = it,
                        modifier = Modifier
                            .width(200.dp)
                            .height(280.dp),
                        onClick = { }
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun CompactScreen(component: HomeComponent) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        ContentView(padding, component)
    }
}

@Composable
fun BigScreen(component: HomeComponent) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        ContentView(PaddingValues(), component)
    }
}