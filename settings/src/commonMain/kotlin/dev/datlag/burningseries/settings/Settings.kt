package dev.datlag.burningseries.settings

import dev.datlag.burningseries.settings.model.AppSettings
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.burningseries.settings.model.UserSettings
import kotlinx.coroutines.flow.Flow
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore

data object Settings {

    interface PlatformAppSettings {
        val all: Flow<AppSettings>
        val language: Flow<Language?>
        val startCounter: Flow<Int>

        suspend fun updateAll(value: AppSettings)
        suspend fun setLanguage(language: Language)
        suspend fun increaseStartCounter()
    }

    @OptIn(ExperimentalOpenIdConnect::class)
    abstract class PlatformUserSettings : TokenStore() {
        abstract val all: Flow<UserSettings>

        abstract suspend fun updateAll(value: UserSettings)
    }
}