package dev.datlag.burningseries.ui.screen.initial.series.activate

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
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
        val dialogState by component.dialog.subscribeAsState()

        Column(
            modifier = Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dialogState.child?.instance?.render()
            WebView(
                url = BSUtil.getBurningSeriesLink(component.episode.href),
                scrapingJs = component.scrapingJs,
                modifier = Modifier.fillMaxSize(),
                onScraped = { data ->
                    component.onScraped(data)
                }
            )
        }
    }
}