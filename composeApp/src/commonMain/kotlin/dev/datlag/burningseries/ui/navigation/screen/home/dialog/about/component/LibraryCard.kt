package dev.datlag.burningseries.ui.navigation.screen.home.dialog.about.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.entity.Library
import dev.datlag.burningseries.other.Constants
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.colorScheme
import dev.datlag.tooling.compose.platform.typography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryCard(
    library: Library
) {
    val uriHandler = LocalUriHandler.current
    val website = library.website?.ifBlank { null } ?: library.scm?.url?.ifBlank { null }

    ElevatedCard(
        onClick = {
            if (!website.isNullOrBlank()) {
                uriHandler.openUri(website)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !website.isNullOrBlank(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1F),
                    text = library.name,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    fontWeight = FontWeight.Medium
                )
                library.artifactVersion?.let {
                    Text(
                        text = it,
                        style = Platform.typography().bodySmall
                    )
                }
            }
            Text(
                text = library.organization?.name?.ifBlank { null } ?: library.developers.map { it.name }.joinToString(),
                style = Platform.typography().bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
            ) {
                library.licenses.forEach { lic ->
                    val url = lic.url?.ifBlank { null } ?: lic.spdxId?.ifBlank { null }?.let { "${Constants.SPDX_LICENSE_BASE}$it" }

                    SuggestionChip(
                        onClick = {
                            if (!url.isNullOrBlank()) {
                                uriHandler.openUri(url)
                            }
                        },
                        enabled = !url.isNullOrBlank(),
                        label = {
                            Text(text = lic.name)
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Platform.colorScheme().tertiary,
                            labelColor = Platform.colorScheme().onTertiary
                        ),
                        border = null
                    )
                }
            }
        }
    }
}