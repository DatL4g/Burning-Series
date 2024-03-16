package dev.datlag.burningseries.shared.ui.screen.initial

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.pages.Pages
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.datlag.burningseries.shared.LocalHaze
import dev.datlag.burningseries.shared.LocalPaddingValues
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.rememberIsTv
import dev.datlag.burningseries.shared.ui.custom.ExpandedPages
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun InitialScreen(component: InitialComponent) {
    val haze = remember { HazeState() }

    CompositionLocalProvider(
        LocalHaze provides haze
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (calculateWindowSizeClass().widthSizeClass) {
                WindowWidthSizeClass.Compact -> CompactScreen(component)
                WindowWidthSizeClass.Medium -> MediumScreen(component)
                WindowWidthSizeClass.Expanded -> {
                    if (rememberIsTv()) {
                        MediumScreen(component)
                    } else {
                        ExpandedScreen(component)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class, ExperimentalFoundationApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
private fun CompactScreen(
    component: InitialComponent
) {
    val selectedPage by component.selectedPage.subscribeAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.hazeChild(
                    state = LocalHaze.current,
                    style = HazeMaterials.thin(NavigationBarDefaults.containerColor)
                ).fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.contentColorFor(NavigationBarDefaults.containerColor)
            ) {
                component.pagerItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedPage == index,
                        icon = {
                            NavIcon(
                                selected = selectedPage == index,
                                item = item
                            )
                        },
                        onClick = {
                            component.selectPage(index)
                        },
                        label = {
                            Text(text = stringResource(item.label))
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) {
        CompositionLocalProvider(
            LocalPaddingValues provides it
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val sponsorScrollEnabled by component.sponsorScrollEnabled.collectAsStateWithLifecycle()
                val homeScrollEnabled by component.homeScrollEnabled.collectAsStateWithLifecycle()
                val favoriteScrollEnabled by component.favoriteScrollEnabled.collectAsStateWithLifecycle()

                Pages(
                    pages = component.pages,
                    onPageSelected = { index ->
                        if (selectedPage != index) {
                            component.selectPage(index)
                        }
                    },
                    pager = { modifier, state, key, pageContent ->
                        val scrollEnabled = when (state.currentPage) {
                            0 -> sponsorScrollEnabled
                            1 -> homeScrollEnabled
                            2 -> favoriteScrollEnabled
                            else -> sponsorScrollEnabled && homeScrollEnabled && favoriteScrollEnabled
                        }
                        HorizontalPager(
                            modifier = modifier,
                            state = state,
                            key = key,
                            pageContent = pageContent,
                            userScrollEnabled = scrollEnabled
                        )
                    }
                ) { _, page ->
                    page.render()
                }
            }
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun MediumScreen(
    component: InitialComponent
) {
    val selectedPage by component.selectedPage.subscribeAsState()

    Scaffold {
        Row(modifier = Modifier.padding(it)) {
            NavigationRail {
                Spacer(modifier = Modifier.weight(1F))
                component.pagerItems.forEachIndexed { index, item ->
                    NavigationRailItem(
                        selected = selectedPage == index,
                        icon = {
                            NavIcon(
                                selected = selectedPage == index,
                                item = item
                            )
                        },
                        onClick = {
                            component.selectPage(index)
                        },
                        label = {
                            Text(text = stringResource(item.label))
                        },
                        alwaysShowLabel = true
                    )
                }
                Spacer(modifier = Modifier.weight(1F))
            }

            ExpandedPages(
                pages = component.pages
            ) { _, page ->
                page.render()
            }
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun ExpandedScreen(
    component: InitialComponent
) {
    val selectedPage by component.selectedPage.subscribeAsState()

    Scaffold {
        PermanentNavigationDrawer(
            modifier = Modifier.padding(it),
            drawerContent = {
                PermanentDrawerSheet(
                    drawerShape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 0.dp
                    )
                ) {
                    Spacer(modifier = Modifier.weight(1F))
                    component.pagerItems.forEachIndexed { index, item ->
                        NavigationDrawerItem(
                            selected = selectedPage == index,
                            icon = {
                                NavIcon(
                                    selected = selectedPage == index,
                                    item = item
                                )
                            },
                            onClick = {
                                component.selectPage(index)
                            },
                            label = {
                                Text(text = stringResource(item.label))
                            }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1F))
                }
            }
        ) {
            ExpandedPages(
                pages = component.pages
            ) { _, page ->
                page.render()
            }
        }
    }
}

@Composable
private fun NavIcon(
    selected: Boolean,
    item: InitialComponent.PagerItem
) {
    Icon(
        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
        contentDescription = stringResource(item.label),
        modifier = Modifier.size(24.dp)
    )
}