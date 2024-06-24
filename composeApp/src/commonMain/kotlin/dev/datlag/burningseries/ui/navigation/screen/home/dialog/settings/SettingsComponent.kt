package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings

import dev.datlag.burningseries.settings.model.Language
import dev.datlag.burningseries.ui.navigation.DialogComponent
import kotlinx.coroutines.flow.Flow

interface SettingsComponent : DialogComponent {

    val language: Flow<Language?>

    fun setLanguage(language: Language)
}