package dev.datlag.burningseries.datastore.common

import androidx.datastore.core.DataStore
import dev.datlag.burningseries.datastore.preferences.AppSettings
import dev.datlag.burningseries.datastore.preferences.AppSettings.Appearance
import dev.datlag.burningseries.datastore.preferences.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val DataStore<UserSettings>.username: Flow<String>
    get() = this.data.map { it.burningSeries.username }

val DataStore<UserSettings>.password: Flow<String>
    get() = this.data.map { it.burningSeries.password }

val DataStore<UserSettings>.loginCookieExpiration: Flow<Long>
    get() = this.data.map { it.burningSeries.loginCookieExpiration }

val DataStore<UserSettings>.showedLogin: Flow<Boolean>
    get() = this.data.map { it.burningSeries.showedLogin }

suspend fun DataStore<UserSettings>.updateBSAccount(
    username: String? = null,
    password: String? = null,
    loginCookieName: String? = null,
    loginCookieValue: String? = null,
    loginCookieMaxAge: Long? = null,
    loginCookieExpiration: Long? = null,
    uidCookieName: String? = null,
    uidCookieValue: String? = null,
    uidCookieMaxAge: Long? = null,
    uidCookieExpiration: Long? = null,
    showedLogin: Boolean? = null
): UserSettings {
    return this.updateData {
        it.toBuilder().setBurningSeries(
            it.burningSeries.toBuilder().setUsername(
                username ?: it.burningSeries.username
            ).setPassword(
                password ?: it.burningSeries.password
            ).setLoginCookieName(
                loginCookieName ?: it.burningSeries.loginCookieName
            ).setLoginCookieValue(
                loginCookieValue ?: it.burningSeries.loginCookieValue
            ).setLoginCookieMaxAge(
                loginCookieMaxAge ?: it.burningSeries.loginCookieMaxAge
            ).setLoginCookieExpiration(
                loginCookieExpiration ?: it.burningSeries.loginCookieExpiration
            ).setUidCookieName(
                uidCookieName ?: it.burningSeries.uidCookieName
            ).setUidCookieValue(
                uidCookieValue ?: it.burningSeries.uidCookieValue
            ).setUidCookieMaxAge(
                uidCookieMaxAge ?: it.burningSeries.uidCookieMaxAge
            ).setUidCookieExpiration(
                uidCookieExpiration ?: it.burningSeries.uidCookieExpiration
            ).setShowedLogin(
                showedLogin ?: it.burningSeries.showedLogin
            ).build()
        ).build()
    }
}

val DataStore<AppSettings>.appearance: Flow<Appearance>
    get() = this.data.map { it.appearance }

val DataStore<AppSettings>.appearanceThemeMode: Flow<Int>
    get() = this.data.map { it.appearance.themeMode }

val DataStore<AppSettings>.appearanceAmoled: Flow<Boolean>
    get() = this.data.map { it.appearance.amoled }

suspend fun DataStore<AppSettings>.updateAppearance(
    themeMode: Int? = null,
    amoled: Boolean? = null
): AppSettings {
    return this.updateData {
        it.toBuilder().setAppearance(
            it.appearance.toBuilder().setThemeMode(
                themeMode ?: it.appearance.themeMode
            ).setAmoled(
                amoled ?: it.appearance.amoled
            ).build()
        ).build()
    }
}
