package dev.datlag.burningseries.ui.navigation.screen.medium.dialog.sponsor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.close
import dev.datlag.burningseries.composeapp.generated.resources.sponsor
import dev.datlag.burningseries.composeapp.generated.resources.sponsor_text
import dev.datlag.burningseries.composeapp.generated.resources.sponsor_hint
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.resources.stringResource
import dev.datlag.burningseries.MokoRes
import dev.datlag.burningseries.common.rememberIsTv
import dev.datlag.burningseries.composeapp.generated.resources.login
import dev.datlag.tooling.Platform
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle

@Composable
fun SponsorDialog(component: SponsorComponent) {
    val isDesktopOrTv = Platform.isDesktop || Platform.rememberIsTv()
    val isLoggedIn by component.isLoggedIn.collectAsStateWithLifecycle(false)

    AlertDialog(
        onDismissRequest = {},
        icon = {
            Icon(
                imageVector = Icons.Rounded.Savings,
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(Res.string.sponsor))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.sponsor_text),
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.sponsor_hint),
                    textAlign = TextAlign.Center
                )
            }
        },
        dismissButton = if (!isDesktopOrTv && !isLoggedIn) {
            {
                TextButton(
                    onClick = {
                        component.login()
                    }
                ) {
                    Image(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        painter = painterResource(MokoRes.images.github),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(Res.string.login))
                }
            }
        } else null,
        confirmButton = {
            TextButton(
                onClick = {
                    component.dismiss()
                }
            ) {
                Text(text = stringResource(Res.string.close))
            }
        }
    )
}