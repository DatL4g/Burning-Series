package dev.datlag.burningseries.other

import dev.datlag.tooling.async.suspendCatching
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
    private val tokenStore: TokenStore
) {

    private val refreshHandler = TokenRefreshHandler(tokenStore)

    suspend fun login(authFlow: CodeAuthFlow): AccessTokenResponse? {
        val access = suspendCatching {
            authFlow.getAccessToken()
        }.getOrNull()

        access?.let {
            tokenStore.saveTokens(it)
        }

        return access
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
}