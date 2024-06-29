package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.other.Constants
import dev.datlag.tooling.compose.onClick

@Composable
fun GitHubOwnerSection(
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = ButtonDefaults.MinHeight)
            .clip(MaterialTheme.shapes.medium)
            .onClick {
                uriHandler.openUri(Constants.GITHUB_OWNER)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Code,
            contentDescription = null,
        )
        Text(text = "Developed by DatLag")
    }
}