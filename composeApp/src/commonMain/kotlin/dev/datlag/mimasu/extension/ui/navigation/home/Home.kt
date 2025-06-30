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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.datlag.mimasu.extension.composeapp.generated.resources.Res
import dev.datlag.mimasu.extension.composeapp.generated.resources.app_name
import dev.datlag.mimasu.extension.composeapp.generated.resources.bs
import dev.datlag.mimasu.extension.composeapp.generated.resources.burning_series
import dev.datlag.mimasu.extension.composeapp.generated.resources.github
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_features_text
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_integration
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_integration_text
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_services
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_services_list
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_updates
import dev.datlag.mimasu.extension.composeapp.generated.resources.home_updates_text
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.mimasu.extension.ui.custom.GitHubButton
import dev.datlag.tooling.Platform
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
    navigateToBurningSeries: () -> Unit
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
        floatingActionButton = {
            if (!Platform.rememberIsTv(anyOS = true)) {
                val reachable by BurningSeries.reachable.collectAsStateWithLifecycle()

                AnimatedVisibility(
                    visible = reachable,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            navigateToBurningSeries()
                        },
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = vectorResource(Res.drawable.bs),
                                contentDescription = null
                            )
                        },
                        text = { Text(text = stringResource(Res.string.burning_series)) }
                    )
                }
            }
        },
        containerColor = Platform.colorScheme().background,
        contentColor = Platform.colorScheme().onBackground
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PlatformText(
                    modifier = Modifier.fillParentMaxWidth().padding(16.dp),
                    text = stringResource(Res.string.home_features_text),
                    softWrap = true,
                    textAlign = TextAlign.Center
                )
            }
            item {
                PlatformText(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                    text = stringResource(Res.string.home_services),
                    style = Platform.typography().titleMedium
                )
            }
            item {
                PlatformText(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                        ),
                    text = stringArrayResource(Res.array.home_services_list).joinToString(separator = "\n") {
                        "- $it"
                    },
                    softWrap = true
                )
            }
            item {
                PlatformText(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                    text = stringResource(Res.string.home_integration),
                    style = Platform.typography().titleMedium
                )
            }
            item {
                PlatformText(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                    text = stringResource(Res.string.home_integration_text),
                    softWrap = true,
                    textAlign = TextAlign.Center
                )
            }
            item {
                PlatformText(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                    text = stringResource(Res.string.home_updates),
                    style = Platform.typography().titleMedium
                )
            }
            item {
                PlatformText(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                    text = stringResource(Res.string.home_updates_text),
                    softWrap = true
                )
            }
            item {
                GitHubButton(
                    onClick = {

                    },
                    modifier = Modifier.fillParentMaxWidth().padding(horizontal = 16.dp),
                    text = stringResource(Res.string.github)
                )
            }
        }
    }
}