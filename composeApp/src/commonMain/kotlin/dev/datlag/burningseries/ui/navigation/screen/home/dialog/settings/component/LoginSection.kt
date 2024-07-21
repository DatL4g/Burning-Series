package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotInterested
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import dev.datlag.tooling.compose.onClick
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import dev.datlag.burningseries.MokoRes
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.login
import dev.datlag.burningseries.composeapp.generated.resources.logout
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.shapes
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginSection(
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = ButtonDefaults.MinHeight)
            .clip(Platform.shapes().small)
            .onClick {
                if (isLoggedIn) {
                    onLogout()
                } else {
                    onLogin()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoggedIn) {
            Icon(
                imageVector = Icons.Rounded.NotInterested,
                contentDescription = null
            )
            Text(text = stringResource(Res.string.logout))
        } else {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(MokoRes.images.github),
                contentDescription = null,
                colorFilter = ColorFilter.tint(LocalContentColor.current)
            )
            Text(text = stringResource(Res.string.login))
        }
    }
}