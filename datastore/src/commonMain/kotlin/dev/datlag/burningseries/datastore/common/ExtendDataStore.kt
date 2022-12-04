package dev.datlag.burningseries.datastore.common

import androidx.datastore.core.DataStore
import dev.datlag.burningseries.datastore.preferences.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val DataStore<UserSettings>.username: Flow<String>
    get() = this.data.map { it.burningSeries.username }

val DataStore<UserSettings>.password: Flow<String>
    get() = this.data.map { it.burningSeries.password }

val DataStore<UserSettings>.cookieExpiration: Flow<Long>
    get() = this.data.map { it.burningSeries.cookieExpiration }

val DataStore<UserSettings>.showedLogin: Flow<Boolean>
    get() = this.data.map { it.burningSeries.showedLogin }

suspend fun DataStore<UserSettings>.updateBSAccount(
    username: String? = null,
    password: String? = null,
    cookieName: String? = null,
    cookieValue: String? = null,
    cookieMaxAge: Long? = null,
    cookieExpiration: Long? = null,
    showedLogin: Boolean? = null
): UserSettings {
    return this.updateData {
        it.toBuilder().setBurningSeries(
            it.burningSeries.toBuilder().setUsername(
                username ?: it.burningSeries.username
            ).setPassword(
                password ?: it.burningSeries.password
            ).setCookieName(
                cookieName ?: it.burningSeries.cookieName
            ).setCookieValue(
                cookieValue ?: it.burningSeries.cookieValue
            ).setCookieMaxAge(
                cookieMaxAge ?: it.burningSeries.cookieMaxAge
            ).setCookieExpiration(
                cookieExpiration ?: it.burningSeries.cookieExpiration
            ).setShowedLogin(
                showedLogin ?: it.burningSeries.showedLogin
            ).build()
        ).build()
    }
}
