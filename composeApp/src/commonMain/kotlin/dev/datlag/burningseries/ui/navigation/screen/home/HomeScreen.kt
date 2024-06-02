package dev.datlag.burningseries.ui.navigation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.common.merge
import dev.datlag.burningseries.ui.custom.AndroidFixWindowSize
import dev.datlag.burningseries.ui.navigation.screen.component.HidingNavigationBar
import dev.datlag.burningseries.ui.navigation.screen.component.NavigationBarState

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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = paddingValues.merge(16.dp)
    ) {
        items(5) {
            Text(text = "Item $it")
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun CompactScreen(component: HomeComponent) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            HidingNavigationBar(
                visible = true,
                selected = NavigationBarState.Home,
                onSponsor = { },
                onHome = { },
                onFavorite = { }
            )
        }
    ) { padding ->
        ContentView(padding, component)
    }
}

@Composable
fun BigScreen(component: HomeComponent) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(it)
        ) {
            NavigationRail {
                Spacer(modifier = Modifier.weight(1F))
                NavigationRailItem(
                    selected = false,
                    onClick = { },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Savings,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(text = "Sponsor")
                    }
                )
                NavigationRailItem(
                    selected = true,
                    onClick = { },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(text = "Home")
                    }
                )
                Spacer(modifier = Modifier.weight(1F))
            }
            ContentView(PaddingValues(), component)
        }
    }
}