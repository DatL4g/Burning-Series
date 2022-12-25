package dev.datlag.burningseries.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalResources
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.other.Constants
import dev.datlag.burningseries.other.EmptyInputStream
import dev.datlag.burningseries.other.Resources
import dev.datlag.burningseries.ui.custom.OverflowMenu
import dev.datlag.burningseries.ui.custom.DropdownMenuItem
import dev.datlag.burningseries.ui.custom.RoundTabs
import dev.datlag.burningseries.ui.custom.SVGImage
import java.io.InputStream

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    val scaffoldState = rememberScaffoldState()
    val resources = LocalResources.current
    val strings = LocalStringRes.current

    fun loadGitHubIcon(): InputStream {
        return resources.getResourcesAsInputStream(Resources.GITHUB_ICON) ?: EmptyInputStream
    }

    val _githubIconInput = remember { loadGitHubIcon() }
    val githubIconInput = if (_githubIconInput.available() > 0) _githubIconInput else loadGitHubIcon()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(
                            text = strings.appName,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            strings.openInBrowser(Constants.GITHUB_REPOSITORY_URL)
                        }) {
                            SVGImage(
                                stream = githubIconInput,
                                description = strings.githubRepository,
                                scale = ContentScale.Inside,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        OverflowMenu(MaterialTheme.colorScheme.onTertiary) {
                            DropdownMenuItem(onClick = {

                            }, enabled = true, text = {
                                Text(
                                    text = strings.settings
                                )
                            }, icon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = strings.settings
                                )
                            })
                            DropdownMenuItem(onClick = {
                                component.onAboutClicked()
                            }, enabled = true, text = {
                                Text(
                                    text = "About"
                                )
                            }, icon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null
                                )
                            })
                        }
                    },
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    elevation = 0.dp
                )
                RoundTabs(listOf(strings.episodes, strings.seriesPlural), component.childIndex)
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                component.onSearchClicked()
            }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }
        },
        scaffoldState = scaffoldState
    ) {
        HomeViewPager(component)
    }
}

@Composable
expect fun gridCellSize(): GridCells
