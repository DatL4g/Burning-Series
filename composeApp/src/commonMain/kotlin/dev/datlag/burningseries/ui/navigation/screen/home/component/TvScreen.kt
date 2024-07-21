package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.k2kast_connection_code
import dev.datlag.burningseries.composeapp.generated.resources.k2kast_default_home
import dev.datlag.burningseries.composeapp.generated.resources.k2kast_experimental
import dev.datlag.burningseries.composeapp.generated.resources.k2kast_loading
import dev.datlag.burningseries.composeapp.generated.resources.k2kast_support
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.burningseries.other.K2Kast
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.ui.navigation.screen.home.HomeComponent
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.PlatformButton
import dev.datlag.tooling.compose.platform.PlatformIcon
import dev.datlag.tooling.compose.platform.PlatformText
import dev.datlag.tooling.compose.platform.localContentColor
import dev.datlag.tooling.compose.platform.typography
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import dev.datlag.tooling.safeSubString
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.stringResource

@Composable
fun TvScreen(
    component: HomeComponent
) {
    val state by component.k2KastState.collectAsStateWithLifecycle(EpisodeState.None)
    val series by component.k2KastSeries.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (val current = state) {
            is EpisodeState.SuccessStream -> {
                component.watch(current.episode, current.results)
            }
            else -> { }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isTvLoading) {
            item {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(
                                ProgressIndicatorDefaults.CircularStrokeWidth.times(2)
                            )
                            .clip(CircleShape),
                        model = series?.coverHref,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.matchParentSize(),
                        color = Platform.localContentColor()
                    )
                }
            }
            item {
                PlatformText(
                    text = series?.mainTitle ?: stringResource(Res.string.k2kast_loading),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            item {
                PlatformIcon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Rounded.Cast,
                    contentDescription = null
                )
            }
            item {
                PlatformText(
                    text = stringResource(Res.string.k2kast_connection_code),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                PlatformText(
                    text = K2Kast.code.safeSubString(0, 3) + " " + K2Kast.code.safeSubString(3, 6),
                    textAlign = TextAlign.Center,
                    style = Platform.typography().headlineLarge
                )
            }
            item {
                PlatformText(
                    modifier = Modifier.fillParentMaxWidth(0.7F).padding(top = 32.dp),
                    text = stringResource(Res.string.k2kast_experimental),
                    textAlign = TextAlign.Center
                )
            }
            item {
                PlatformText(
                    modifier = Modifier.fillParentMaxWidth(0.7F),
                    text = stringResource(Res.string.k2kast_support),
                    textAlign = TextAlign.Center
                )
            }
            item {
                PlatformButton(
                    onClick = {
                        K2Kast.hide()
                        StateSaver.defaultHome.update { true }
                    }
                ) {
                    PlatformIcon(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    PlatformText(
                        text = stringResource(Res.string.k2kast_default_home)
                    )
                }
            }
        }
    }
}