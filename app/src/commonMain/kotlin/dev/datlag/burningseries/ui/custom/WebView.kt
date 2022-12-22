package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import dev.datlag.burningseries.ui.screen.activate.ActivateComponent
import okhttp3.OkHttpClient

@Composable
expect fun WebView(component: ActivateComponent)