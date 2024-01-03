package dev.datlag.burningseries.shared.ui.screen.initial.home.component

import androidx.compose.foundation.lazy.grid.LazyGridScope
import dev.datlag.burningseries.model.Release
import kotlinx.coroutines.flow.StateFlow

expect fun LazyGridScope.DeviceContent(release: StateFlow<Release?>, onDeviceReachable: StateFlow<Boolean>)