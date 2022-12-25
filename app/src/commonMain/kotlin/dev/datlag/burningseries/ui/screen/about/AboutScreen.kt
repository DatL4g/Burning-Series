package dev.datlag.burningseries.ui.screen.about

import androidx.compose.material3.*
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.ui.custom.Libraries

@Composable
fun AboutScreen(component: AboutComponent) {
    val strings = LocalStringRes.current

    Scaffold(topBar = {
        TopAppBar(
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            elevation = 0.dp,
            navigationIcon = {
                IconButton(onClick = {
                    component.onGoBack()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            },
            title = {
                Text(
                    text = LocalStringRes.current.appName,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        )
    }) {
        Libraries { url ->
            url?.let { strings.openInBrowser(it) }
        }
    }
}