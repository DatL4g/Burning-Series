package dev.datlag.mimasu.extension.ui.navigation.burningseries

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import dev.datlag.mimasu.extension.composeapp.generated.resources.Res
import dev.datlag.mimasu.extension.composeapp.generated.resources.burning_series_activate
import dev.datlag.mimasu.extension.composeapp.generated.resources.burning_series_activate_subtitle
import dev.datlag.mimasu.extension.composeapp.generated.resources.burning_series_error
import dev.datlag.mimasu.extension.composeapp.generated.resources.burning_series_preparing
import dev.datlag.mimasu.extension.composeapp.generated.resources.burning_series_success
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.mimasu.extension.ui.navigation.WebViewClient
import dev.datlag.mimasu.extension.viewmodel.BurningSeriesViewModel
import dev.datlag.mimasu.extension.viewmodel.kodeinViewModel
import dev.datlag.tooling.Platform
import dev.datlag.tooling.async.launchIO
import dev.datlag.tooling.async.withMainContext
import dev.datlag.tooling.compose.platform.colorScheme
import dev.datlag.tooling.compose.platform.typography
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun BurningSeries(onBack: () -> Unit) {
    val viewModel = kodeinViewModel<BurningSeriesViewModel>()
    val snackbarState = remember { SnackbarHostState() }
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val saveState by viewModel.state.collectAsStateWithLifecycle()
    val successText = stringResource(Res.string.burning_series_success)
    val errorText = stringResource(Res.string.burning_series_error)

    LaunchedEffect(saveState, successText, errorText) {
        when (saveState) {
            is BurningSeriesViewModel.State.Error -> {
                snackbarState.showSnackbar(
                    message = errorText,
                    withDismissAction = true
                )
            }
            is BurningSeriesViewModel.State.Saved -> {
                snackbarState.showSnackbar(
                    message = successText,
                    withDismissAction = true
                )
            }
            else -> { }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                    ) {
                        Text(
                            text = stringResource(Res.string.burning_series_activate),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text(
                            text = stringResource(Res.string.burning_series_activate_subtitle),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = Platform.typography().labelMedium
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = saveState is BurningSeriesViewModel.State.Saving,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = null
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Platform.colorScheme().surface,
                    contentColor = Platform.colorScheme().onSurface,
                    dismissActionContentColor = Platform.colorScheme().onSurface
                )
            }
        }
    ) { contentPadding ->
        val state = rememberWebViewState(BurningSeries.homePage)
        val scope = rememberCoroutineScope()

        if (enabled) {
            WebView(
                state = state,
                modifier = Modifier.padding(contentPadding).fillMaxSize(),
                captureBackPresses = true,
                client = WebViewClient(
                    allowedHosts = setOf(BurningSeries.HOST)
                ),
                onCreated = {
                    it.settings.allowFileAccess = false
                    it.settings.javaScriptEnabled = true
                    it.settings.javaScriptCanOpenWindowsAutomatically = false
                    it.settings.mediaPlaybackRequiresUserGesture = true

                    scope.launchIO {
                        it.scrape(
                            js = Res.readBytes("files/scrape_hoster.js").decodeToString(),
                            onScraped = { episodeHref, result ->
                                viewModel.saveScraped(episodeHref, result)
                            }
                        )
                    }
                }
            )
        } else {
            Column(
                modifier = Modifier.padding(contentPadding).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(Res.string.burning_series_preparing))
            }
        }
    }
}

private suspend fun WebView.scrape(js: String?, onScraped: (String?, String?) -> Unit) {
    while (currentCoroutineContext().isActive && !js.isNullOrBlank()) {
        delay(3000)
        withMainContext {
            val episodeHref = BurningSeries.matchingUrl(
                this@scrape.url,
                this@scrape.originalUrl
            )

            this@scrape.evaluateJavascript(js) { result ->
                val episode = episodeHref ?: BurningSeries.matchingUrl(
                    this@scrape.url,
                    this@scrape.originalUrl
                )

                onScraped(episode, result)
            }
        }
    }
}