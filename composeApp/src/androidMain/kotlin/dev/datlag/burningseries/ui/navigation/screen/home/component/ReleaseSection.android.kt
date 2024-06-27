package dev.datlag.burningseries.ui.navigation.screen.home.component

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalDI
import dev.datlag.burningseries.MokoRes
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.burningseries.other.DownloadManager
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.withMainContext
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.painterResource
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import ru.solrudev.ackpine.session.SessionResult

@OptIn(DelicateCoroutinesApi::class)
@Composable
actual fun ReleaseSection(
    release: UserAndRelease.Release,
    modifier: Modifier,
    onHide: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val progress by DownloadManager.progress.collectAsStateWithLifecycle()

        Text(
            text = release.title ?: release.tagName,
            style = MaterialTheme.typography.headlineMedium
        )
        Text("A new version is available")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val uriHandler = LocalUriHandler.current
            val context = LocalContext.current

            release.url?.let { viewUrl ->
                OutlinedButton(
                    modifier = Modifier.weight(1F),
                    onClick = {
                        uriHandler.openUri(viewUrl)
                    }
                ) {
                    Image(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        painter = painterResource(MokoRes.images.github),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = "View")
                }
            }
            release.androidAsset?.let { asset ->
                Button(
                    modifier = Modifier.weight(1F),
                    onClick = {
                        GlobalScope.launchIO {
                            val file = DownloadManager.download(asset) ?: return@launchIO

                            when (DownloadManager.install(context, file)) {
                                is SessionResult.Success -> {
                                    withMainContext {
                                        onHide()
                                    }
                                }
                                is SessionResult.Error -> {
                                    release.url?.let {
                                        uriHandler.openUri(it)
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = "Install")
                }
            }
        }
    }
}