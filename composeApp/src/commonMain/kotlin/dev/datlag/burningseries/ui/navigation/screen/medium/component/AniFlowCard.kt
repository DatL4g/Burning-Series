package dev.datlag.burningseries.ui.navigation.screen.medium.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.other.AniFlow
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.PlatformCard
import dev.datlag.tooling.compose.platform.PlatformText
import dev.datlag.tooling.compose.platform.shapes
import dev.datlag.tooling.compose.platform.typography
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AniFlowCard(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    PlatformCard(
        modifier = modifier,
        onClick = {
            uriHandler.openUri(AniFlow.googlePlay)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                modifier = Modifier.size(48.dp).clip(Platform.shapes().medium),
                painter = painterResource(AniFlow.icon),
                contentDescription = stringResource(AniFlow.title)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformText(
                    text = stringResource(AniFlow.title),
                    fontWeight = FontWeight.Bold,
                    style = Platform.typography().titleLarge
                )
                PlatformText(text = stringResource(AniFlow.subTitle))
            }
        }
    }
}