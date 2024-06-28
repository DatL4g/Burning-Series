package dev.datlag.burningseries.ui.navigation.screen.home.dialog.release

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.MokoRes
import dev.datlag.burningseries.common.drawProgress
import dev.datlag.burningseries.common.rememberIsTv
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.burningseries.other.Constants
import dev.datlag.burningseries.other.DownloadManager
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.withMainContext
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.painterResource
import ru.solrudev.ackpine.session.SessionResult

@Composable
actual fun ReleaseDialog(component: ReleaseComponent) {
    val androidAsset = component.release.androidAsset
    val isDraftOrPreRelease = remember(component.release) { component.release.isDraft || component.release.isPrerelease }
    var dismissRequests by remember(isDraftOrPreRelease) { mutableIntStateOf(if (isDraftOrPreRelease) 5 else 0) }

    AlertDialog(
        onDismissRequest = {
            dismissRequests++
            if (dismissRequests >= 5) {
                component.dismiss()
            }
        },
        icon = {
            Icon(
                imageVector = DeviceIcon,
                contentDescription = null
            )
        },
        title = {
            Text(text = component.release.title ?: component.release.tagName)
        },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "A new version is available ${component.release.tagName}\nYou should check it out!",
                textAlign = TextAlign.Center
            )
        },
        dismissButton = if (androidAsset != null) {
            {
                ViewButton(component.release)
            }
        } else null,
        confirmButton = {
            if (androidAsset == null) {
                ViewButton(component.release)
            } else {
                val progress by DownloadManager.progress.collectAsStateWithLifecycle()
                val installEnabled by DownloadManager.downloadEnabled.collectAsStateWithLifecycle()
                val uriHandler = LocalUriHandler.current
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                TextButton(
                    onClick = {
                        scope.launchIO {
                            val file = DownloadManager.download(androidAsset) ?: return@launchIO

                            when (DownloadManager.install(context, file)) {
                                is SessionResult.Success -> {
                                    withMainContext {
                                        component.dismiss()
                                    }
                                }
                                is SessionResult.Error -> {
                                    uriHandler.openUri(component.release.url ?: Constants.GITHUB_RELEASE)
                                }
                            }
                        }
                    },
                    enabled = installEnabled,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.textButtonColors(
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .defaultMinSize(
                                minWidth = ButtonDefaults.MinWidth,
                                minHeight = ButtonDefaults.MinHeight
                            )
                            .clip(CircleShape)
                            .drawProgress(LocalContentColor.current, progress.percentage)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
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
    )
}

@Composable
private fun ViewButton(release: UserAndRelease.Release) {
    val uriHandler = LocalUriHandler.current

    TextButton(
        onClick = {
            uriHandler.openUri(release.url ?: Constants.GITHUB_RELEASE)
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

internal actual val DeviceIcon: ImageVector
    @Composable
    get() = if (Platform.rememberIsTv()) {
        Icons.Rounded.SystemUpdateAlt
    } else {
        Icons.Rounded.SystemUpdate
    }