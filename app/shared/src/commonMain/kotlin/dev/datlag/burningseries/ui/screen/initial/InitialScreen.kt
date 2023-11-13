package dev.datlag.burningseries.ui.screen.initial

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.pages.Pages
import com.moriatsushi.insetsx.ExperimentalSoftwareKeyboardApi
import com.moriatsushi.insetsx.safeDrawingPadding
import dev.datlag.burningseries.ui.custom.ExpandedPages
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun InitialScreen(component: InitialComponent) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (calculateWindowSizeClass().widthSizeClass) {
            WindowWidthSizeClass.Compact -> CompactScreen(component)
            WindowWidthSizeClass.Medium -> MediumScreen(component)
            WindowWidthSizeClass.Expanded -> ExpandedScreen(component)
        }
    }
}

@OptIn(ExperimentalSoftwareKeyboardApi::class, ExperimentalDecomposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun CompactScreen(
    component: InitialComponent
) {
    var selectedPage by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                component.pagerItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedPage == index,
                        icon = {
                            NavIcon(item)
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
        Box(modifier = Modifier.padding(it)) {
            Pages(
                pages = component.pages,
                onPageSelected = { index ->
                    component.selectPage(index)
                }
            ) { index, page ->
                selectedPage = index
                page.render()
            }
        }
    }
}

@OptIn(ExperimentalSoftwareKeyboardApi::class, ExperimentalDecomposeApi::class)
@Composable
private fun MediumScreen(
    component: InitialComponent
) {
    var selectedPage by remember { mutableIntStateOf(0) }

    Scaffold {
        Row(modifier = Modifier.padding(it)) {
            NavigationRail {
                Spacer(modifier = Modifier.weight(1F))
                component.pagerItems.forEachIndexed { index, item ->
                    NavigationRailItem(
                        selected = selectedPage == index,
                        icon = {
                            NavIcon(item)
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
            ) { index, page ->
                selectedPage = index
                page.render()
            }
        }
    }
}

@OptIn(ExperimentalSoftwareKeyboardApi::class, ExperimentalDecomposeApi::class)
@Composable
private fun ExpandedScreen(
    component: InitialComponent
) {
    var selectedPage by remember { mutableIntStateOf(0) }

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
                                NavIcon(item)
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
            ) { index, page ->
                selectedPage = index
                page.render()
            }
        }
    }
}

@Composable
private fun NavIcon(item: InitialComponent.PagerItem) {
    Icon(
        imageVector = item.icon,
        contentDescription = stringResource(item.label),
        modifier = Modifier.size(24.dp)
    )
}