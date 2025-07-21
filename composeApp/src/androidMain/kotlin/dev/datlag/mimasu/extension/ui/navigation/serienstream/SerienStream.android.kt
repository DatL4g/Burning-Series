package dev.datlag.mimasu.extension.ui.navigation.serienstream

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import dev.datlag.mimasu.extension.composeapp.generated.resources.Res
import dev.datlag.mimasu.extension.composeapp.generated.resources.serienstream
import dev.datlag.mimasu.extension.ui.navigation.WebViewClient
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun SerienStream(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                    ) {
                        Text(
                            text = stringResource(Res.string.serienstream),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
            )
        },
    ) { contentPadding ->
        WebView(
            state = rememberWebViewState("https://s.to"),
            modifier = Modifier.padding(contentPadding).fillMaxSize(),
            captureBackPresses = true,
            client = WebViewClient(
                allowedHosts = setOf(
                    "s.to",
                    "serienstream.to"
                )
            ),
            onCreated = {
                it.settings.allowFileAccess = false
                it.settings.javaScriptEnabled = true
                it.settings.javaScriptCanOpenWindowsAutomatically = false
                it.settings.mediaPlaybackRequiresUserGesture = true
            }
        )
    }
}