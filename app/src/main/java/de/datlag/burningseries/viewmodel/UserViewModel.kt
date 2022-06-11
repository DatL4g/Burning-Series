package de.datlag.burningseries.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kttdevelopment.mal4j.MyAnimeList
import com.kttdevelopment.mal4j.anime.AnimePreview
import com.kttdevelopment.mal4j.anime.property.AnimeStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.model.Constants
import de.datlag.model.burningseries.series.EpisodeInfo
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.model.github.User
import de.datlag.network.anilist.AniListRepository
import de.datlag.network.anilist.MediaQuery
import de.datlag.network.anilist.ViewerQuery
import de.datlag.network.anilist.type.MediaListStatus
import de.datlag.network.burningseries.BurningSeriesRepository
import de.datlag.network.github.GitHubRepository
import de.datlag.network.myanimelist.MyAnimeListRepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserDenyList
import net.openid.appauth.browser.BrowserMatcher
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
@Obfuscate
class UserViewModel @Inject constructor(
    val bsRepository: BurningSeriesRepository,
    val malRepository: MyAnimeListRepository,
    val anilistRepository : AniListRepository,
    val gitHubRepository: GitHubRepository,
    @Named("malClientId") val malClientId: String,
    @Named("anilistClientId") val anilistClientId: String,
    @Named("anilistClientSecret") val anilistClientSecret: String,
    @Named("githubClientId") val githubClientId: String,
    @Named("githubClientSecret") val githubClientSecret: String
) : ViewModel() {

    private var malServiceConfig = AuthorizationServiceConfiguration(
        Constants.MAL_OAUTH_AUTH_URI.toUri(),
        Constants.MAL_OAUTH_TOKEN_URI.toUri()
    )
    private var malAuthState = AuthState(malServiceConfig)
    private var malAuthService: AuthorizationService? = null
    private var saveMalAuthListener: ((String) -> Unit)? = null

    private var anilistServiceConfig = AuthorizationServiceConfiguration(
        Constants.ANILIST_OAUTH_AUTH_URI.toUri(),
        Constants.ANILIST_OAUTH_TOKEN_URI.toUri()
    )
    private var anilistAuthState = AuthState(anilistServiceConfig)
    private var anilistAuthService : AuthorizationService? = null
    private var saveAniListListener: ((String) -> Unit)? = null

    private var githubServiceConfig = AuthorizationServiceConfiguration(
        Constants.GITHUB_OAUTH_AUTH_URI.toUri(),
        Constants.GITHUB_OAUTH_TOKEN_URI.toUri()
    )
    private var githubAuthState = AuthState(githubServiceConfig)
    private var githubAuthService: AuthorizationService? = null
    private var saveGitHubAuthListener: ((String) -> Unit)? = null

    private val appAuthConfig = AppAuthConfiguration.Builder()
        .setBrowserMatcher(BrowserDenyList(
            *Constants.OAUTH_BROWSER_DENY.map { deny ->
                BrowserMatcher {
                    it.packageName.equals(deny, true)
                }
            }.toTypedArray()
        )).build()

    fun malResultLauncherCallback(result: ActivityResult?) {
        if (result?.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val resp = data?.let { AuthorizationResponse.fromIntent(it) }
            val ex = AuthorizationException.fromIntent(data)
            malAuthState.update(resp, ex)

            if (!resp?.authorizationCode.isNullOrEmpty()) {
                resp?.createTokenExchangeRequest()?.let {
                    malAuthService?.performTokenRequest(it) { response, tokenEx ->
                        malAuthState.update(response, tokenEx)
                        saveMalAuthListener?.invoke(malAuthState.jsonSerializeString())
                    }
                }
            }
        }
    }

    fun aniListResultLauncherCallback(result: ActivityResult?) {
        if (result?.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val resp = data?.let { AuthorizationResponse.fromIntent(it) }
            val ex = AuthorizationException.fromIntent(data)
            anilistAuthState.update(resp, ex)

            if (!resp?.authorizationCode.isNullOrEmpty()) {
                resp?.createTokenExchangeRequest()?.let {
                    val clientAuth = ClientSecretBasic(anilistClientSecret)
                    anilistAuthService?.performTokenRequest(it, clientAuth) { response, tokenEx ->
                        anilistAuthState.update(response, tokenEx)
                        saveAniListListener?.invoke(anilistAuthState.jsonSerializeString())
                    }
                }
            }
        }
    }

    fun githubResultLauncherCallback(result: ActivityResult?) {
        if (result?.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val resp = data?.let { AuthorizationResponse.fromIntent(it) }
            val ex = AuthorizationException.fromIntent(data)
            githubAuthState.update(resp, ex)

            if (!resp?.authorizationCode.isNullOrEmpty()) {
                resp?.createTokenExchangeRequest()?.let {
                    val clientAuth = ClientSecretBasic(githubClientSecret)
                    githubAuthService?.performTokenRequest(it, clientAuth) { response, tokenEx ->
                        githubAuthState.update(response, tokenEx)
                        saveGitHubAuthListener?.invoke(githubAuthState.jsonSerializeString())
                    }
                }
            }
        }
    }

    fun createMalAuthIntent(context: Context): Intent? {
        val codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier()
        val authRequest = AuthorizationRequest.Builder(
            malServiceConfig,
            malClientId,
            Constants.MAL_RESPONSE_TYPE,
            Constants.MAL_REDIRECT_URI.toUri()
        )
            .setCodeVerifier(
                codeVerifier,
                codeVerifier,
                AuthorizationRequest.CODE_CHALLENGE_METHOD_PLAIN
            )
            .build()

        malAuthService = AuthorizationService(context, appAuthConfig)
        return malAuthService?.getAuthorizationRequestIntent(authRequest)
    }

    fun createAniListAuthIntent(context: Context): Intent? {
        val authRequest = AuthorizationRequest.Builder(
            anilistServiceConfig,
            anilistClientId,
            Constants.ANILIST_RESPONSE_TYPE,
            Constants.ANILIST_REDIRECT_URI.toUri()
        ).build()

        anilistAuthService = AuthorizationService(context, appAuthConfig)
        return anilistAuthService?.getAuthorizationRequestIntent(authRequest)
    }

    fun createGitHubAuthIntent(context: Context): Intent? {
        val authRequest = AuthorizationRequest.Builder(
            githubServiceConfig,
            githubClientId,
            Constants.GITHUB_RESPONSE_TYPE,

            Constants.GITHUB_REDIRECT_URI.toUri()
        ).setScope("read:user").build()

        githubAuthService = AuthorizationService(context, appAuthConfig)
        return githubAuthService?.getAuthorizationRequestIntent(authRequest)
    }

    fun endMalAuth() {
        malAuthState = AuthState(malServiceConfig)
        saveMalAuthListener?.invoke(String())
    }

    fun endAniListAuth() {
        anilistAuthState = AuthState(anilistServiceConfig)
        saveAniListListener?.invoke(String())
    }

    fun endGitHubAuth() {
        githubAuthState = AuthState(githubServiceConfig)
        saveGitHubAuthListener?.invoke(String())
    }

    fun loadMalAuth(auth: String) {
        if (auth.isNotEmpty()) {
            malAuthState = AuthState.jsonDeserialize(auth)
        }
    }

    fun loadAniListAuth(auth: String) {
        if (auth.isNotEmpty()) {
            anilistAuthState = AuthState.jsonDeserialize(auth)
        }
    }

    fun loadGitHubAuth(auth: String) {
        if (auth.isNotEmpty()) {
            githubAuthState = AuthState.jsonDeserialize(auth)
        }
    }

    fun setSaveMalAuthListener(listener: (String) -> Unit) {
        saveMalAuthListener = listener
    }

    fun setSaveAniListListener(listener: (String) -> Unit) {
        saveAniListListener = listener
    }

    fun setSaveGitHubAuthListener(listener: (String) -> Unit) {
        saveGitHubAuthListener = listener
    }

    fun isMalAuthorized() = malAuthState.isAuthorized

    fun isAniListAuthorized() = anilistAuthState.isAuthorized

    fun isGitHubAuthorized() = githubAuthState.isAuthorized

    fun getUserMal(callback: (mal: MyAnimeList?) -> Unit) {
        fun malWithFreshToken() {
            malAuthService?.let {
                malAuthState.performActionWithFreshTokens(it) { accessToken, _, ex ->
                    if (accessToken.isNullOrEmpty() || ex != null) {
                        callback.invoke(null)
                    } else {
                        callback.invoke(malRepository.getUserMal(accessToken))
                    }
                }
            }
        }

        if (!isMalAuthorized()) {
            callback.invoke(null)
        } else {
            if (malAuthState.needsTokenRefresh) {
                malWithFreshToken()
            } else {
                malAuthState.accessToken?.let {
                    return@let callback.invoke(malRepository.getUserMal(it))
                } ?: malWithFreshToken()
            }
        }
    }

    fun getMalSeries(mal: MyAnimeList?, seriesData: SeriesWithInfo): Flow<AnimePreview?> = flow {
        if (mal != null) {
            emitAll(malRepository.getMalSeries(mal, seriesData))
        } else {
            emit(null)
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    fun syncMalSeries(
        preview: AnimePreview,
        episodes: List<EpisodeInfo>,
        continueEpisodeInfo: EpisodeInfo?,
        isFirstSeason: Boolean,
        isLastSeason: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        val malListStatus = preview.listStatus
        val malWatchedAmount = malListStatus?.watchedEpisodes ?: 0
        val malWatchStatus = malListStatus?.status
        val malRewatching = malListStatus?.isRewatching ?: false
        val malCompletedOrRewatching = malWatchStatus == AnimeStatus.Completed || (malWatchStatus == AnimeStatus.Watching && malRewatching)

        val deviceWatchedAmount = if (continueEpisodeInfo != null) {
            (if (!continueEpisodeInfo.finishedWatching) {
                continueEpisodeInfo.episodeNumber?.minus(1)
            } else {
                continueEpisodeInfo.episodeNumber
            }) ?: episodes.filter { it.finishedWatching }.size
        } else {
            episodes.filter { it.finishedWatching }.size
        }

        if (deviceWatchedAmount > malWatchedAmount) {
            if (deviceWatchedAmount >= episodes.size && isLastSeason) {
                if (malCompletedOrRewatching) {
                    try {
                        malListStatus.edit().episodesWatched(deviceWatchedAmount).update()
                    } catch (ignored: Exception) { }
                } else {
                    try {
                        malListStatus.edit().episodesWatched(deviceWatchedAmount).update().edit().status(AnimeStatus.Completed).update()
                    } catch (ignored: Exception) { }
                }
            } else {
                try {
                    malListStatus.edit().episodesWatched(deviceWatchedAmount).update()
                } catch (ignored: Exception) { }
            }
        } else {
            val addWatchedAmount = malWatchedAmount - deviceWatchedAmount
            if (addWatchedAmount > 0) {
                val notWatchedEpisodes = episodes.filter {
                    val episodeNumber = it.episodeNumber
                    val episodeOrListNumber = it.episodeNumberOrListNumber
                    val episodeNumberInRange = if (episodeNumber != null) {
                        episodeNumber <= malWatchedAmount
                    } else {
                        if (episodeOrListNumber != null && isFirstSeason) {
                            episodeOrListNumber <= malWatchedAmount
                        } else {
                            false
                        }
                    }
                    !it.finishedWatching && episodeNumberInRange
                }
                notWatchedEpisodes.map { async {
                    it.totalWatchPos = Long.MAX_VALUE
                    it.currentWatchPos = Long.MAX_VALUE
                    bsRepository.updateEpisodeInfo(it)
                } }.awaitAll()
            }
        }
    }

    fun getAniListUser(): Flow<ViewerQuery.Viewer?> = flow {
        if (!isAniListAuthorized()) {
            emit(null)
        } else {
            if (anilistAuthState.needsTokenRefresh) {
                aniListFreshToken().firstOrNull()?.let {
                    emitAll(anilistRepository.getViewer(it))
                } ?: emit(null)
            } else {
                anilistAuthState.accessToken?.let {
                    return@let emitAll(anilistRepository.getViewer(it))
                } ?: aniListFreshToken().firstOrNull()?.let {
                    emitAll(anilistRepository.getViewer(it))
                } ?: emit(null)
            }
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    fun getGitHubUser(): Flow<User?> = flow {
        if (!isGitHubAuthorized()) {
            emit(null)
        } else {
            if (githubAuthState.needsTokenRefresh) {
                githubFreshToken().firstOrNull()?.let {
                    emitAll(gitHubRepository.getUser(it))
                } ?: emit(null)
            } else {
                githubAuthState.accessToken?.let {
                    return@let emitAll(gitHubRepository.getUser(it))
                } ?: githubFreshToken().firstOrNull()?.let {
                    emitAll(gitHubRepository.getUser(it))
                } ?: emit(null)
            }
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    fun getGitHubSponsorStatus(user: User): Flow<Boolean> = flow<Boolean> {
        if (!isGitHubAuthorized()) {
            emit(false)
        } else {
            if (githubAuthState.needsTokenRefresh) {
                githubFreshToken().firstOrNull()?.let {
                    emitAll(gitHubRepository.isSponsoring(user.login, it))
                } ?: emit(false)
            } else {
                githubAuthState.accessToken?.let {
                    return@let emitAll(gitHubRepository.isSponsoring(user.login, it))
                } ?: githubFreshToken().firstOrNull()?.let {
                    emitAll(gitHubRepository.isSponsoring(user.login, it))
                } ?: emit(false)
            }
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    private fun aniListFreshToken(): Flow<String?> = flow {
        anilistAuthService?.let {
            return@let anilistAuthState.performActionWithFreshTokens(it) { accessToken, _, ex ->
                if (accessToken.isNullOrEmpty() || ex != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        emit(null)
                    }
                } else {
                    viewModelScope.launch(Dispatchers.IO) {
                        emit(accessToken)
                    }
                }
            }
        } ?: emit(null)
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    private fun githubFreshToken(): Flow<String?> = flow {
        githubAuthService?.let {
            return@let githubAuthState.performActionWithFreshTokens(it) {access, _, ex ->
                if (access.isNullOrEmpty() || ex != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        emit(null)
                    }
                } else {
                    viewModelScope.launch(Dispatchers.IO) {
                        emit(access)
                    }
                }
            }
        } ?: emit(null)
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    fun getAniListSeries(seriesData: SeriesWithInfo): Flow<MediaQuery.Medium?> = flow {
        if (!isAniListAuthorized()) {
            emit(null)
        } else {
            if (anilistAuthState.needsTokenRefresh) {
                aniListFreshToken().firstOrNull()?.let {
                    emitAll(anilistRepository.getAniListSeries(it, seriesData))
                } ?: emit(null)
            } else {
                anilistAuthState.accessToken?.let {
                    return@let emitAll(anilistRepository.getAniListSeries(it, seriesData))
                } ?: aniListFreshToken().firstOrNull()?.let {
                    emitAll(anilistRepository.getAniListSeries(it, seriesData))
                } ?: emit(null)
            }
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    fun syncAniListSeries(
        medium: MediaQuery.Medium,
        episodes: List<EpisodeInfo>,
        continueEpisodeInfo: EpisodeInfo?,
        isFirstSeason: Boolean,
        isLastSeason: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        suspend fun saveOrUpdate(token: String) {
            val aniListStatus = medium.mediaListEntry
            val aniListWatchedAmount = aniListStatus?.progress ?: 0
            val aniListWatchStatus = aniListStatus?.status
            val aniListRewatching = aniListWatchStatus == MediaListStatus.REPEATING || (aniListWatchStatus?.rawValue ?: String()) == MediaListStatus.REPEATING.rawValue
            val aniListCompleted = aniListWatchStatus == MediaListStatus.COMPLETED || (aniListWatchStatus?.rawValue ?: String()) == MediaListStatus.COMPLETED.rawValue
            val aniListCompletedOrRewatching = aniListRewatching || aniListCompleted

            val deviceWatchedAmount = if (continueEpisodeInfo != null) {
                (if (!continueEpisodeInfo.finishedWatching) {
                    continueEpisodeInfo.episodeNumber?.minus(1)
                } else {
                    continueEpisodeInfo.episodeNumber
                }) ?: episodes.filter { it.finishedWatching }.size
            } else {
                episodes.filter { it.finishedWatching }.size
            }

            if (deviceWatchedAmount > aniListWatchedAmount) {
                if (deviceWatchedAmount >= episodes.size && isLastSeason) {
                    if (aniListCompletedOrRewatching) {
                        anilistRepository.saveAniListEntry(token, medium.id, deviceWatchedAmount, aniListWatchStatus ?: MediaListStatus.CURRENT)
                    } else {
                        anilistRepository.saveAniListEntry(token, medium.id, deviceWatchedAmount, MediaListStatus.COMPLETED)
                    }
                } else {
                    anilistRepository.saveAniListEntry(token, medium.id, deviceWatchedAmount, aniListWatchStatus ?: MediaListStatus.CURRENT)
                }
            } else {
                val addWatchedAmount = aniListWatchedAmount - deviceWatchedAmount
                if (addWatchedAmount > 0) {
                    val notWatchedEpisodes = episodes.filter {
                        val episodeNumber = it.episodeNumber
                        val episodeOrListNumber = it.episodeNumberOrListNumber
                        val episodeNumberInRange = if (episodeNumber != null) {
                            episodeNumber <= aniListWatchedAmount
                        } else {
                            if (episodeOrListNumber != null && isFirstSeason) {
                                episodeOrListNumber <= aniListWatchedAmount
                            } else {
                                false
                            }
                        }
                        !it.finishedWatching && episodeNumberInRange
                    }
                    notWatchedEpisodes.map { async {
                        it.totalWatchPos = Long.MAX_VALUE
                        it.currentWatchPos = Long.MAX_VALUE
                        bsRepository.updateEpisodeInfo(it)
                    } }.awaitAll()
                }
            }
        }

        if (isAniListAuthorized()) {
            if (anilistAuthState.needsTokenRefresh) {
                aniListFreshToken().firstOrNull()?.let {
                    saveOrUpdate(it)
                }
            } else {
                anilistAuthState.accessToken?.let {
                    return@let saveOrUpdate(it)
                } ?: aniListFreshToken().firstOrNull()?.let {
                    saveOrUpdate(it)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        malAuthService = null
        anilistAuthService = null
        githubAuthService = null
    }
}