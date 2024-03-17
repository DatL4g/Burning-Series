package dev.datlag.burningseries.shared.ui.screen.initial.series.activate

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.localPadding
import dev.datlag.burningseries.shared.ui.custom.state.UnreachableState
import dev.datlag.burningseries.shared.ui.screen.initial.series.activate.component.WebView
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ActivateScreen(component: ActivateComponent) {
    val dialogState by component.dialog.subscribeAsState()
    val onDeviceReachable = remember { component.onDeviceReachable }

    Column(
        modifier = Modifier.localPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isSaving by component.isSaving.collectAsStateWithLifecycle()

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
            Text(
                modifier = Modifier.weight(1F),
                text = stringResource(SharedRes.strings.activate_hint),
                maxLines = 2
            )
            if (isSaving) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = stringResource(SharedRes.strings.saving)
                    )
                    CircularProgressIndicator()
                }
            }
        }
        if (onDeviceReachable) {
            dialogState.child?.instance?.render()
            WebView(
                url = BSUtil.getBurningSeriesLink(component.episode.href),
                modifier = Modifier.fillMaxSize(),
                onScraped = { data ->
                    component.onScraped(data)
                }
            )
        } else {
            UnreachableState(SharedRes.strings.activate_unreachable)
        }
    }
}