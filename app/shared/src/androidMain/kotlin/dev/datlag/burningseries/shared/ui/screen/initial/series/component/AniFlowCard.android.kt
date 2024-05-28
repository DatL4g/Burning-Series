package dev.datlag.burningseries.shared.ui.screen.initial.series.component

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.common.isPackageInstalled
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.other.Project
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.StateFlow

/**
 * Keep single implementation instead of ProjectCard, to support hint for not connected.
 */
@Composable
actual fun AniFlowCard(
    isAnime: StateFlow<Boolean>,
    modifier: Modifier
) {
    val context = LocalContext.current
    val project = Project.AniFlow

    if (!context.isPackageInstalled(project.`package`)) {
        val anime by isAnime.collectAsStateWithLifecycle()

        if (anime) {
            val uriHandler = LocalUriHandler.current

            Card(
                modifier = modifier,
                onClick = {
                    uriHandler.openUri(project.googlePlay ?: project.github)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    project.icon?.let {
                        Image(
                            modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.medium),
                            painter = painterResource(it),
                            contentDescription = stringResource(project.title),
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(project.title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(text = stringResource(project.subTitle))
                    }
                }
            }
        }
    }
}