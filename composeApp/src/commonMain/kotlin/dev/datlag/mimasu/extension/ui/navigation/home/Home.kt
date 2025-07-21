package dev.datlag.mimasu.extension.ui.navigation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.datlag.mimasu.extension.common.header
import dev.datlag.mimasu.extension.composeapp.generated.resources.Res
import dev.datlag.mimasu.extension.composeapp.generated.resources.aniworld
import dev.datlag.mimasu.extension.composeapp.generated.resources.app_name
import dev.datlag.mimasu.extension.composeapp.generated.resources.bs
import dev.datlag.mimasu.extension.composeapp.generated.resources.burning_series
import dev.datlag.mimasu.extension.composeapp.generated.resources.github
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_content_activate
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_content_not_loading
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_content_not_loading_text
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_features_text
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_integration
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_integration_text
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_services
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_services_list
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_updates
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_updates_text
import dev.datlag.mimasu.extension.composeapp.generated.resources.serienstream
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.mimasu.extension.ui.custom.GitHubButton
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.PlatformCard
import dev.datlag.tooling.compose.platform.PlatformCardColors
import dev.datlag.tooling.compose.platform.PlatformText
import dev.datlag.tooling.compose.platform.colorScheme
import dev.datlag.tooling.compose.platform.rememberIsTv
import dev.datlag.tooling.compose.platform.typography
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navigateToBurningSeries: () -> Unit,
    navigateToAniWorld: () -> Unit,
    navigateToSerienStream: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(
                        text = stringResource(Res.string.app_name),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        containerColor = Platform.colorScheme().background,
        contentColor = Platform.colorScheme().onBackground
    ) { contentPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            header {
                PlatformText(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    text = stringResource(Res.string.home_features_text),
                    softWrap = true,
                    textAlign = TextAlign.Center
                )
            }
            header {
                PlatformText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                    text = stringResource(Res.string.home_services),
                    style = Platform.typography().titleMedium
                )
            }
            header {
                PlatformText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                        ),
                    text = stringArrayResource(Res.array.home_services_list).joinToString(separator = "\n") {
                        "- $it"
                    },
                    softWrap = true
                )
            }
            header {
                val uriHandler = LocalUriHandler.current

                GitHubButton(
                    onClick = {
                        uriHandler.openUri("https://github.com/DatL4g/Mimasu-Extension")
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    text = stringResource(Res.string.github)
                )
            }
            header {
                PlatformText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                    text = stringResource(Res.string.home_content_not_loading),
                    style = Platform.typography().titleMedium
                )
            }
            header {
                PlatformText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                        ),
                    text = stringResource(Res.string.home_content_not_loading_text),
                )
            }
            header {
                PlatformCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        navigateToBurningSeries()
                    },
                    colors = PlatformCardColors.default(
                        containerColor = Color(0xFF0b4576),
                        contentColor = Color.White
                    )
                ) {
                    PlatformText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        text = stringResource(Res.string.burning_series),
                        style = Platform.typography().titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    PlatformText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                            )
                            .padding(bottom = 8.dp),
                        text = stringResource(Res.string.home_content_activate),
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                PlatformCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp),
                    onClick = {
                        navigateToAniWorld()
                    },
                    colors = PlatformCardColors.default(
                        containerColor = Color(0xFF637cf9),
                        contentColor = Color.White
                    )
                ) {
                    PlatformText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                        text = stringResource(Res.string.aniworld),
                        style = Platform.typography().titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                PlatformCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 16.dp),
                    onClick = {
                        navigateToSerienStream()
                    },
                    colors = PlatformCardColors.default(
                        containerColor = Color(0xFF44adf3),
                        contentColor = Color.White
                    )
                ) {
                    PlatformText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                        text = stringResource(Res.string.serienstream),
                        style = Platform.typography().titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}