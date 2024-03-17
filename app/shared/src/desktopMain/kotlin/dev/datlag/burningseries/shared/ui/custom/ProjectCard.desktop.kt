package dev.datlag.burningseries.shared.ui.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.common.openInBrowser
import dev.datlag.burningseries.shared.other.Project
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
actual fun ProjectCard(project: Project, modifier: Modifier) {
    ElevatedCard(
        modifier = modifier,
        onClick = {
            (project.github ?: project.googlePlay)?.openInBrowser()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            project.icon?.let {
                Image(
                    modifier = Modifier.size(64.dp),
                    painter = painterResource(it),
                    contentDescription = stringResource(project.title)
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