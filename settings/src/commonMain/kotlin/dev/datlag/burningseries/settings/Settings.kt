package dev.datlag.burningseries.settings

import dev.datlag.burningseries.settings.model.Language
import kotlinx.coroutines.flow.Flow
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore

data object Settings {

    interface PlatformAppSettings {
        val language: Flow<Language?>
        val startCounter: Flow<Int>

        suspend fun setLanguage(language: Language)
        suspend fun increaseStartCounter()
    }

    @OptIn(ExperimentalOpenIdConnect::class)
    abstract class PlatformUserSettings : TokenStore() {}
}