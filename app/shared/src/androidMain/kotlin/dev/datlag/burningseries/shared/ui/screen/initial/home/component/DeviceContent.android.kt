package dev.datlag.burningseries.shared.ui.screen.initial.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.model.Release
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.header
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.openInBrowser
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.StateFlow

actual fun LazyGridScope.DeviceContent(release: StateFlow<Release?>, onDeviceReachable: StateFlow<Boolean>) {
    header {
        val newRelease by release.collectAsStateWithLifecycle()
        val reachable by onDeviceReachable.collectAsStateWithLifecycle()

        if (newRelease != null) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val context = LocalContext.current

                Column(
                    modifier = Modifier.weight(1F),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(SharedRes.strings.release_available_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = stringResource(SharedRes.strings.release_available_text, newRelease!!.title)
                    )
                }
                Button(
                    onClick = {
                        newRelease!!.htmlUrl.openInBrowser(context)
                    }
                ) {
                    Image(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        painter = painterResource(SharedRes.images.GitHub),
                        contentDescription = stringResource(SharedRes.strings.github),
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(SharedRes.strings.github))
                }
            }
        } else if (!reachable) {
            Text(text = stringResource(SharedRes.strings.enable_custom_dns))
        }
    }
}