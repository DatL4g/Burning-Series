package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.content.ContextCompat
import dev.datlag.burningseries.MokoRes
import dev.datlag.burningseries.common.rememberIsTv
import dev.datlag.burningseries.other.AniFlow
import dev.datlag.burningseries.other.isInstalled
import dev.datlag.tooling.Platform
import dev.icerock.moko.resources.compose.painterResource

@Composable
actual fun AniFlowIconButton(onClick: () -> Unit) {
    if (Platform.rememberIsTv()) {
        IconButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null
            )
        }
    } else {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val installed = AniFlow.isInstalled()

        IconButton(
            onClick = {
                if (installed) {
                    val intent = context.packageManager.getLaunchIntentForPackage(AniFlow.packageName)

                    if (intent != null) {
                        ContextCompat.startActivity(context, intent, null)
                    } else {
                        uriHandler.openUri(AniFlow.googlePlay)
                    }
                } else {
                    uriHandler.openUri(AniFlow.googlePlay)
                }
            }
        ) {
            Image(
                painter = painterResource(MokoRes.images.AniFlow),
                contentDescription = null
            )
        }
    }
}