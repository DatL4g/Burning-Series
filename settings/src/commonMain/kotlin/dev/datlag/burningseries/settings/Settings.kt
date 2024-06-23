package dev.datlag.burningseries.settings

import dev.datlag.burningseries.settings.model.Language
import kotlinx.coroutines.flow.Flow

data object Settings {

    interface PlatformAppSettings {
        val language: Flow<Language?>

        suspend fun setLanguage(language: Language)
    }
}