package dev.datlag.mimasu.extension.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.mimasu.extension.firebase.model.ScrapedData
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.mimasu.extension.kache.asyncPutAndGet
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.tooling.async.launchIO
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours

class BurningSeriesViewModel(
    private val firebaseWrapper: FirebaseWrapper?,
    private val json: Json,
    private val client: HttpClient?
) : ViewModel() {

    private val _enabled = MutableStateFlow(firebaseWrapper != null && firebaseWrapper.auth.isSignedIn)
    val enabled = _enabled.asStateFlow()

    private val _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()

    private val saveKache = InMemoryKache<String, String>(
        maxSize = 2L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
        expireAfterWriteDuration = 12.hours
    }

    private val _homePage = MutableStateFlow(BurningSeries.fallbackHomePage())
    val homePage = _homePage.asStateFlow()

    init {
        if (firebaseWrapper != null) {
            viewModelScope.launchIO {
                val signedIn = firebaseWrapper.auth.signInAnonymously()

                if (signedIn) {
                    firebaseWrapper.auth.signedIn.collect {
                        _enabled.emit(it)
                    }
                }
            }
        }
        viewModelScope.launchIO {
            _homePage.update { BurningSeries.getHomePage(client) }
        }
    }

    fun saveScraped(episodeHref: String?, data: String?) {
        val trimmedData = data?.trim()?.ifBlank { null } ?: return
        val episode = episodeHref?.trim()?.ifBlank { null } ?: return
        val firebase = firebaseWrapper ?: return

        viewModelScope.launchIO {
            val alreadySaved = !saveKache.async(episode)?.ifBlank { null }.isNullOrBlank()

            if (!alreadySaved) {
                if (!trimmedData.equals("null", ignoreCase = true) && !trimmedData.equals("undefined", ignoreCase = true)) {
                    _state.emit(State.Saving)
                    val converted = suspendCatching {
                        json.decodeFromString<ScrapedData>(trimmedData)
                    }.getOrNull()

                    if (converted != null) {
                        if (firebase.store.addStream(converted)) {
                            _state.emit(State.Saved)
                            saveKache.asyncPutAndGet(episode, converted.url)
                        } else {
                            _state.emit(State.Error)
                        }
                    } else {
                        _state.emit(State.Error)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launchIO {
            firebaseWrapper?.auth?.signOut()
        }

        super.onCleared()
    }

    @Serializable
    sealed interface State {

        @Serializable
        data object Saving : State

        @Serializable
        data object Saved : State

        @Serializable
        data object Error : State
    }
}