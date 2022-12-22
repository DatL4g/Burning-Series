package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.datlag.burningseries.ui.screen.activate.ActivateComponent
import okhttp3.OkHttpClient

@Composable
actual fun WebView(component: ActivateComponent) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Activating is not supported on desktop yet.")
    }
}