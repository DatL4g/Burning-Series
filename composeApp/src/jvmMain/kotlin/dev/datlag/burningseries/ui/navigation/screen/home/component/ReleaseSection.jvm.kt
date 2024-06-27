package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.datlag.burningseries.github.model.UserAndRelease
import io.ktor.client.HttpClient

@Composable
actual fun ReleaseSection(
    release: UserAndRelease.Release,
    modifier: Modifier,
    onHide: () -> Unit
) {
    Text(
        text = release.title ?: release.tagName
    )
}