package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import dev.datlag.tooling.Platform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual val Platform.linksSupported: StateFlow<Boolean>
    get() = MutableStateFlow(false)

actual fun LazyListScope.LinkSupportSection(headerPadding: PaddingValues) { }