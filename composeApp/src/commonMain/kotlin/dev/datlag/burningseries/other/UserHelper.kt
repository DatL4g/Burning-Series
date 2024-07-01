package dev.datlag.burningseries.other

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.common.countWatchedEpisodes
import dev.datlag.burningseries.github.GitHub
import dev.datlag.burningseries.github.UserAndReleaseQuery
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.burningseries.settings.Settings
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.compose.ioDispatcher
import io.github.aakira.napier.Napier
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.saveTokens
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

@OptIn(ExperimentalOpenIdConnect::class)
class UserHelper(
    private val github: GitHub,
    private val appVersion: String?,
    private val oidcClient: OpenIdConnectClient,
    private val tokenStore: TokenStore,
    private val appSettings: Settings.PlatformAppSettings,
    private val database: BurningSeries
) {

    private val client = ApolloClient.Builder()
        .dispatcher(ioDispatcher())
        .serverUrl("https://api.github.com/graphql")
        .addHttpInterceptor(object : HttpInterceptor {
            override suspend fun intercept(
                request: HttpRequest,
                chain: HttpInterceptorChain
            ): HttpResponse {
                val req = request.newBuilder().apply {
                    val token = getAccessToken()

                    token?.let {
                        addHeader("Authorization", "Bearer $it")
                    }
                }.build()

                return chain.proceed(req)
            }
        })
        .build()

    private val refreshHandler = TokenRefreshHandler(tokenStore)

    private val query = Query()
    private val tokenDate = MutableStateFlow(Clock.System.now().epochSeconds)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userAndRelease = tokenDate.transformLatest {
        return@transformLatest emitAll(
            client.query(query.toGraphQL()).toFlow()
        )
    }.map {
        it.data?.let(::UserAndRelease)
    }.map {
        it ?: suspendCatching {
            github.getLatestRelease(query.owner, query.repo)
        }.getOrNull()?.let(::UserAndRelease)
    }.map { data ->
        if (data?.user != null) {
            _user.update { data.user }
        }
        val latestRelease = data?.release?.asUpdateOrNull(appVersion)
        if (latestRelease != null) {
            _release.update { latestRelease }
        }
        data
    }.flowOn(
        context = ioDispatcher()
    ).stateIn(
        scope = GlobalScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private val _user: MutableStateFlow<UserAndRelease.User?> = MutableStateFlow(null)
    val user: StateFlow<UserAndRelease.User?> = _user

    private val _release: MutableStateFlow<UserAndRelease.Release?> = MutableStateFlow(null)
    val release: StateFlow<UserAndRelease.Release?> = _release

    val isSponsoring: Boolean
        get() = user.value?.isSponsoring == true

    suspend fun login(authFlow: CodeAuthFlow): AccessTokenResponse? {
        val access = suspendCatching {
            authFlow.getAccessToken()
        }.getOrNull()

        access?.let {
            tokenStore.saveTokens(it)
            tokenDate.update { Clock.System.now().epochSeconds }
        }

        return access
    }

    suspend fun logout() {
        tokenStore.saveTokens(
            accessToken = "",
            refreshToken = null,
            idToken = null
        )
        _user.update { null }
        tokenDate.update { Clock.System.now().epochSeconds }
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

    private data class Query(
        val owner: String = Constants.GITHUB_OWNER_NAME,
        val repo: String = Constants.GITHUB_REPO_NAME
    ) {
        fun toGraphQL() = UserAndReleaseQuery(
            owner = owner,
            repo = repo
        )
    }
}