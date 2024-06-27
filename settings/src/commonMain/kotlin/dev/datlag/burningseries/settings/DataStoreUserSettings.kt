package dev.datlag.burningseries.settings

import androidx.datastore.core.DataStore
import dev.datlag.burningseries.settings.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DataStoreUserSettings(
    private val dataStore: DataStore<UserSettings>
) : Settings.PlatformUserSettings() {
    override val accessTokenFlow: Flow<String?> = dataStore.data.map { it.github.accessToken?.ifBlank { null } }
    override val idTokenFlow: Flow<String?> = dataStore.data.map { it.github.idToken?.ifBlank { null } }
    override val refreshTokenFlow: Flow<String?> = dataStore.data.map { it.github.refreshToken?.ifBlank { null } }

    override suspend fun getAccessToken(): String? {
        return dataStore.data.map { it.github.accessToken?.ifBlank { null } }.firstOrNull()
    }

    override suspend fun getIdToken(): String? {
        return dataStore.data.map { it.github.idToken?.ifBlank { null } }.firstOrNull()
    }

    override suspend fun getRefreshToken(): String? {
        return dataStore.data.map { it.github.refreshToken?.ifBlank { null } }.firstOrNull()
    }

    override suspend fun removeAccessToken() {
        dataStore.updateData {
            it.copy(
                github = it.github.copy(
                    accessToken = null
                )
            )
        }
    }

    override suspend fun removeIdToken() {
        dataStore.updateData {
            it.copy(
                github = it.github.copy(
                    idToken = null
                )
            )
        }
    }

    override suspend fun removeRefreshToken() {
        dataStore.updateData {
            it.copy(
                github = it.github.copy(
                    refreshToken = null
                )
            )
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?) {
        dataStore.updateData {
            it.copy(
                github = it.github.copy(
                    idToken = idToken,
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
        }
    }
}