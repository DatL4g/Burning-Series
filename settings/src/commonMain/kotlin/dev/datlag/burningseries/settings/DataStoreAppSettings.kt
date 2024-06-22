package dev.datlag.burningseries.settings

import androidx.datastore.core.DataStore
import dev.datlag.burningseries.settings.model.AppSettings
import dev.datlag.burningseries.settings.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreAppSettings(
    private val dataStore: DataStore<AppSettings>
) : Settings.PlatformAppSettings {

    override val language: Flow<Language?> = dataStore.data.map { it.language }
    override val customFonts: Flow<Boolean> = dataStore.data.map { it.customFonts }

    override suspend fun setLanguage(language: Language) {
        dataStore.updateData {
            it.copy(
                language = language
            )
        }
    }

    override suspend fun useCustomFonts(value: Boolean) {
        dataStore.updateData {
            it.copy(
                customFonts = value
            )
        }
    }
}