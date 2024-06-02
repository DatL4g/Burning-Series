package dev.datlag.burningseries.ui.navigation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
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

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun CompactScreen(component: HomeComponent) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.hazeChild(
                    state = LocalHaze.current,
                    style = HazeMaterials.thin(NavigationBarDefaults.containerColor)
                ).fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.contentColorFor(NavigationBarDefaults.containerColor),
            ) {
                NavigationBarItem(
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
                NavigationBarItem(
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
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = padding.merge(16.dp)
        ) {
            items(5) {
                Text(text = "Item $it")
            }
        }
    }
}

@Composable
fun BigScreen(component: HomeComponent) {
    Text(text = "Big/Wide Screen")
}