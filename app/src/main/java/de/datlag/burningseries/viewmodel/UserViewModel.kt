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
import de.datlag.network.burningseries.BurningSeriesRepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.*
import net.openid.appauth.*
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
@Obfuscate
class UserViewModel @Inject constructor(
    val repository: BurningSeriesRepository,
    @Named("malClientId") val malClientId: String
) : ViewModel() {

    private var malServiceConfig = AuthorizationServiceConfiguration(
        Constants.MAL_OAUTH_AUTH_URI.toUri(),
        Constants.MAL_OAUTH_TOKEN_URI.toUri()
    )
    private var malAuthState = AuthState(malServiceConfig)
    private var malAuthService: AuthorizationService? = null
    private var saveMalAuthListener: ((String) -> Unit)? = null

    fun resultLauncherCallback(result: ActivityResult?) {
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
        malAuthService = AuthorizationService(context)
        return malAuthService?.getAuthorizationRequestIntent(authRequest)
    }

    fun endMalAuth() {
        malAuthState = AuthState(malServiceConfig)
        saveMalAuthListener?.invoke(String())
    }

    fun loadMalAuth(auth: String) {
        if (auth.isNotEmpty()) {
            malAuthState = AuthState.jsonDeserialize(auth)
        }
    }

    fun setSaveMalAuthListener(listener: (String) -> Unit) {
        saveMalAuthListener = listener
    }

    fun isMalAuthorized() = malAuthState.isAuthorized

    fun getUserMal(callback: (mal: MyAnimeList?) -> Unit) {
        if (!isMalAuthorized()) {
            callback.invoke(null)
        }
        if (malAuthState.needsTokenRefresh) {
            malAuthService?.let {
                malAuthState.performActionWithFreshTokens(it) { accessToken, idToken, ex ->
                    if (accessToken.isNullOrEmpty() || ex != null) {
                        callback.invoke(null)
                    } else {
                        callback.invoke(MyAnimeList.withToken("Bearer $accessToken"))
                    }
                }
            }
        } else {
            callback.invoke(MyAnimeList.withToken("Bearer ${malAuthState.accessToken}"))
        }
    }

    fun getMalSeries(seriesData: SeriesWithInfo, callback: (AnimePreview?) -> Unit) {
        getUserMal { getMalSeries(it, seriesData, callback) }
    }

    fun getMalSeries(mal: MyAnimeList?, seriesData: SeriesWithInfo, callback: (AnimePreview?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        mal?.let {
            if (seriesData.infos.any { info -> info.data.contains("Anime", true) }) {
                val query = it.anime.includeNSFW().withLimit(1)
                val malWithSeason = query.withQuery("${seriesData.series.title} ${seriesData.series.season}").search() ?: listOf()
                val malEntry: AnimePreview? = malWithSeason.firstOrNull() ?: (query.withQuery(seriesData.series.title).search() ?: listOf()).firstOrNull()

                withContext(Dispatchers.Main) {
                    callback.invoke(malEntry)
                }
            } else {
                callback.invoke(null)
            }
        } ?: callback.invoke(null)
    }

    fun syncMalSeries(preview: AnimePreview, episodes: List<EpisodeInfo>) = viewModelScope.launch(Dispatchers.IO) {
        val malListStatus = preview.listStatus
        val malWatchedAmount = malListStatus?.watchedEpisodes ?: 0
        val malWatchStatus = malListStatus?.status
        val malRewatching = malListStatus?.isRewatching ?: false
        val malCompletedOrRewatching = malWatchStatus == AnimeStatus.Completed || (malWatchStatus == AnimeStatus.Watching && malRewatching)

        val deviceWatchedAmount = episodes.filter { it.watchedPercentage() >= 90F }.size

        if (deviceWatchedAmount > malWatchedAmount) {
            if (deviceWatchedAmount >= episodes.size) {
                if (malCompletedOrRewatching) {
                    malListStatus.edit().episodesWatched(deviceWatchedAmount).update()
                } else {
                    malListStatus.edit().episodesWatched(deviceWatchedAmount).update().edit().status(AnimeStatus.Completed).update()
                }
            } else {
                malListStatus.edit().episodesWatched(deviceWatchedAmount).update()
            }
        } else {
            val addWatchedAmount = malWatchedAmount - deviceWatchedAmount
            if (addWatchedAmount > 0) {
                val notWatchedEpisodes = episodes.filterNot { it.watchedPercentage() >= 90F }.toMutableList().subList(0, addWatchedAmount)
                notWatchedEpisodes.map { async {
                    it.totalWatchPos = Long.MAX_VALUE
                    it.currentWatchPos = Long.MAX_VALUE
                    repository.updateEpisodeInfo(it)
                } }.awaitAll()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        malAuthService = null
    }
}