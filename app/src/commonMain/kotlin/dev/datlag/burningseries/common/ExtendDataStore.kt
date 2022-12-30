package dev.datlag.burningseries.common

import androidx.datastore.core.DataStore
import dev.datlag.burningseries.datastore.preferences.UserSettings
import dev.datlag.burningseries.other.Constants
import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val DataStore<UserSettings>.cookieMap: Flow<List<Pair<String, String>>>
    get() = this.data.map { settings ->
        val list: MutableList<Pair<String, String>> = mutableListOf()

        if (!settings.burningSeries.loginCookieName.isNullOrEmpty()
            && !settings.burningSeries.loginCookieValue.isNullOrEmpty()) {
            list.add(settings.burningSeries.loginCookieName to settings.burningSeries.loginCookieValue)
        }

        if (!settings.burningSeries.uidCookieName.isNullOrEmpty()
            && !settings.burningSeries.uidCookieValue.isNullOrEmpty()) {
            list.add(settings.burningSeries.uidCookieName to settings.burningSeries.uidCookieValue)
        }

        list
    }