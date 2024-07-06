package dev.datlag.burningseries.settings

import androidx.datastore.core.DataStore
import dev.datlag.burningseries.settings.model.AppSettings
import dev.datlag.burningseries.settings.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreAppSettings(
    private val dataStore: DataStore<AppSettings>
) : Settings.PlatformAppSettings {

    override val all: Flow<AppSettings> = dataStore.data
    override val language: Flow<Language?> = all.map { it.language }
    override val startCounter: Flow<Int> = all.map { it.startCounter }

    // Don't just override with value as we want to keep startCounter device specific
    override suspend fun updateAll(value: AppSettings) {
        dataStore.updateData {
            it.copy(
                language = value.language
            )
        }
    }

    override suspend fun setLanguage(language: Language) {
        dataStore.updateData {
            it.copy(
                language = language
            )
        }
    }

    override suspend fun increaseStartCounter() {
        dataStore.updateData {
            val current = it.startCounter
            it.copy(
                startCounter = if (current == Int.MAX_VALUE) {
                    current
                } else {
                    current + 1
                }
            )
        }
    }
}