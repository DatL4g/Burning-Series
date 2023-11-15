package dev.datlag.burningseries.ui.screen.initial.home.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.LocalRestartRequired
import dev.datlag.burningseries.common.header
import dev.datlag.burningseries.other.CEFState
import dev.datlag.burningseries.other.LocalCEFInitialization
import dev.datlag.burningseries.SharedRes
import dev.datlag.burningseries.window.ApplicationDisposer
import dev.icerock.moko.resources.compose.stringResource

actual fun LazyGridScope.DeviceContent() {
    header {
        val cefInitState by LocalCEFInitialization.current
        val restartRequired = LocalRestartRequired.current

        if (restartRequired) {
            val disposer = ApplicationDisposer.current

            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(SharedRes.strings.restart_required_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = stringResource(SharedRes.strings.restart_required_text)
                    )
                }
                Spacer(modifier = Modifier.weight(1F))
                Button(
                    onClick = {
                        disposer.restart()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = stringResource(SharedRes.strings.restart)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(SharedRes.strings.restart))
                }
            }
        } else {
            when (val current = cefInitState) {
                is CEFState.Downloading -> {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(SharedRes.strings.downloading),
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(
                            text = stringResource(SharedRes.strings.downloading_text)
                        )

                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().clip(CircleShape),
                            progress = current.progress / 100F
                        )
                    }
                }
                else -> { }
            }
        }
    }
}