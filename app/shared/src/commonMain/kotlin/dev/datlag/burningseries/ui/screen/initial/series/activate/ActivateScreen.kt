package dev.datlag.burningseries.ui.screen.initial.series.activate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import dev.datlag.burningseries.SharedRes
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.ui.screen.initial.series.activate.component.WebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivateScreen(component: ActivateComponent) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(SharedRes.strings.activate_hint))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.back()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = stringResource(SharedRes.strings.back)
                        )
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            WebView(
                url = BSUtil.getBurningSeriesLink(component.episode.href),
                scrapingJs = component.scrapingJs,
                modifier = Modifier.fillMaxSize()
            ) { data ->
                component.onScraped(data)
            }
        }
    }
}