package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalEdgeToEdge
import dev.datlag.burningseries.common.isFullyExpandedOrTargeted
import dev.datlag.burningseries.common.merge
import dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings.component.InfoSection
import dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings.component.LanguageSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(component: SettingsComponent) {
    val sheetState = rememberModalBottomSheetState()
    val (insets, bottomPadding) = if (LocalEdgeToEdge.current) {
        WindowInsets(
            left = 0,
            top = 0,
            right = 0,
            bottom = 0
        ) to BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom).asPaddingValues()
    } else {
        BottomSheetDefaults.windowInsets to PaddingValues()
    }

    ModalBottomSheet(
        onDismissRequest = component::dismiss,
        windowInsets = insets,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = bottomPadding.merge(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                InfoSection(
                    dismissVisible = sheetState.isFullyExpandedOrTargeted(forceFullExpand = true),
                    modifier = Modifier.fillParentMaxWidth(),
                    onDismiss = component::dismiss
                )
            }
            item {
                LanguageSection(
                    languageFlow = component.language,
                    modifier = Modifier.fillParentMaxWidth(),
                    onSelect = component::setLanguage
                )
            }
        }
    }
}