package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.datlag.burningseries.github.model.UserAndRelease
import io.ktor.client.HttpClient

@Composable
expect fun ReleaseSection(
    release: UserAndRelease.Release,
    modifier: Modifier = Modifier,
    onHide: () -> Unit
)