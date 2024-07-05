package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Phonelink
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.alwaysEnabledColors
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.syncing_enabled
import dev.datlag.burningseries.other.DomainVerifier
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource

actual fun LazyListScope.SyncSection() {
    if (DomainVerifier.supported) {
        item {
            val context = LocalContext.current
            val enabled by DomainVerifier.syncingEnabled.collectAsStateWithLifecycle()

            SideEffect {
                DomainVerifier.verify(context)
            }

            Row(
                modifier = Modifier.fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Phonelink,
                    contentDescription = null,
                )
                Text(text = stringResource(Res.string.syncing_enabled))
                Spacer(modifier = Modifier.weight(1F))
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        DomainVerifier.enable(context)
                    },
                    enabled = !enabled,
                    colors = SwitchDefaults.alwaysEnabledColors(),
                    thumbContent = {
                        if (enabled) {
                            Icon(
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    }
}