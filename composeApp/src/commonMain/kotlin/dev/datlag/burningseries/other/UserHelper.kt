package dev.datlag.burningseries.other

import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.common.countWatchedEpisodes
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.burningseries.settings.Settings
import dev.datlag.tooling.async.suspendCatching
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.saveTokens
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

@OptIn(ExperimentalOpenIdConnect::class)
class UserHelper(
    private val oidcClient: OpenIdConnectClient,
    private val tokenStore: TokenStore,
    private val appSettings: Settings.PlatformAppSettings,
    private val database: BurningSeries
) {

    private val refreshHandler = TokenRefreshHandler(tokenStore)
    private var user: UserAndRelease.User? = null
        set(value) {
            field = value
            _isLoggedIn.update { value != null }
        }

    private val _isLoggedIn = MutableStateFlow(user != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    val isSponsoring: Boolean
        get() = user?.isSponsoring ?: false

    suspend fun login(authFlow: CodeAuthFlow): AccessTokenResponse? {
        val access = suspendCatching {
            authFlow.getAccessToken()
        }.getOrNull()

        access?.let {
            tokenStore.saveTokens(it)
        }

        return access
    }

    suspend fun logout() {
        tokenStore.saveTokens(
            accessToken = "",
            refreshToken = null,
            idToken = null
        )
        user = null
    }

    suspend fun getAccessWithAuthFlowToken(authFlow: CodeAuthFlow): String? {
        val oldToken = tokenStore.getAccessToken()

        if (oldToken.isNullOrBlank()) {
            return login(authFlow)?.access_token?.ifBlank { null } ?: oldToken
        }

        val token = suspendCatching {
            withTimeout(2000) {
                refreshHandler.refreshAndSaveToken(oidcClient, oldToken)
            }
        }.getOrNull()

        if (token == null) {
            return login(authFlow)?.access_token?.ifBlank { null } ?: oldToken
        }

        return token.accessToken.ifBlank { null }
    }

    suspend fun getAccessToken(): String? {
        val oldToken = tokenStore.getAccessToken() ?: return null

        val token = suspendCatching(catchTimeout = true) {
            withTimeout(2000) {
                refreshHandler.refreshAndSaveToken(oidcClient, oldToken)
            }
        }.getOrNull()

        return token?.accessToken?.ifBlank { null } ?: oldToken
    }

    suspend fun requiresSponsoring(): Boolean {
        if ((appSettings.startCounter.firstOrNull() ?: 0) >= 15) {
            return true
        }

        val watched = database.countWatchedEpisodes()
        return watched >= 30
    }

    fun updateUser(value: UserAndRelease.User) {
        user = value
    }
}