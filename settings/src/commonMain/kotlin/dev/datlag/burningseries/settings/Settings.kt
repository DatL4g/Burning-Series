package dev.datlag.burningseries.settings

import dev.datlag.burningseries.settings.model.Language
import kotlinx.coroutines.flow.Flow

data object Settings {

    interface PlatformAppSettings {
        val language: Flow<Language?>
        val customFonts: Flow<Boolean>

        suspend fun setLanguage(language: Language)
        suspend fun useCustomFonts(value: Boolean)
    }
}