package dev.datlag.mimasu.extension.service

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.datlag.mimasu.extension.IShowInfoProvider
import dev.datlag.mimasu.extension.provider.EpisodeManager
import dev.datlag.mimasu.extension.provider.SearchManager
import dev.datlag.mimasu.extension.provider.model.Show
import dev.datlag.mimasu.extension.show.EpisodeCallback
import dev.datlag.mimasu.extension.show.ShowCallback
import dev.datlag.mimasu.extension.show.StreamCallback
import dev.datlag.tooling.safeCast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instanceOrNull

class ShowService : LifecycleService() {

    override fun onBind(intent: Intent): IBinder? {
        val result = super.onBind(intent)

        val di = applicationContext.safeCast<DIAware>()?.di
            ?: (application as? DIAware)?.di
            ?: return result

        return Binder(lifecycleScope, di)
    }

    class Binder(
        val scope: CoroutineScope,
        override val di: DI
    ) : IShowInfoProvider.Stub(), DIAware {

        init {
            Log.e("ShowService", "Binding service for shows")
        }

        private val _searchManager by instanceOrNull<SearchManager>()
        private val searchManager: SearchManager?
            get() = _searchManager ?: run {
                val newInstance by instanceOrNull<SearchManager>()
                newInstance
            }

        private val _episodeManager by instanceOrNull<EpisodeManager>()
        private val episodeManager: EpisodeManager?
            get() = _episodeManager ?: run {
                val newInstance by instanceOrNull<EpisodeManager>()
                newInstance
            }

        init {
            scope.launch(Dispatchers.IO) {
                searchManager?.initialize()
            }
        }

        override fun requestShowId(request: ByteArray?, callback: ShowCallback?) {
            val manager = searchManager ?: return run {
                Log.e("ShowService", "No Search Manager")
            }

            scope.launch(Dispatchers.IO) {
                val requestInfo = Show.Request(request) ?: return@launch run {
                    Log.e("ShowService", "No valid RequestInfo")
                }
                val id = manager.search(requestInfo) ?: return@launch run {
                    Log.e("ShowService", "No ID found for RequestInfo")
                }
                Log.e("ShowService", "Found ID: $id")
                callback?.onResult(id)
            }
        }

        override fun requestEpisodeAvailability(
            showId: Int,
            request: ByteArray?,
            callback: EpisodeCallback?
        ) {
            val showHolder = searchManager ?: return run {
                Log.e("ShowService", "No Search Manager for Episode")
            }
            val manager = episodeManager ?: return run {
                Log.e("ShowService", "No Episode Manager")
            }

            scope.launch(Dispatchers.IO) {
                val requestInfo = Show.EpisodeRequest(request) ?: return@launch run {
                    Log.e("ShowService", "Episode RequestInfo invalid")
                }
                val showInfo = showHolder.matchedShowResults(showId) ?: return@launch run {
                    Log.e("ShowService", "No Matched ShowInfo")
                }
                val available = manager.episodeAvailability(
                    showId = showId,
                    matchedShowResults = showInfo,
                    request = requestInfo
                )
                Log.e("ShowService", "Available: $available")

                callback?.onResult(available)
            }
        }

        override fun requestStream(showId: Int, request: ByteArray?, callback: StreamCallback?) {
            val showHolder = searchManager ?: return run {
                Log.e("ShowService", "No Search Manager for Stream")
                callback?.onResult(null)
            }
            val manager = episodeManager ?: return run {
                Log.e("ShowService", "No Episode Manager for Stream")
                callback?.onResult(null)
            }

            scope.launch(Dispatchers.IO) {
                val requestInfo = Show.EpisodeRequest(request) ?: return@launch run {
                    Log.e("ShowService", "Invalid EpisodeRequest for Stream")
                    callback?.onResult(null)
                }
                val showInfo = showHolder.matchedShowResults(showId) ?: return@launch run {
                    Log.e("ShowService", "No Matched Show for Stream")
                    callback?.onResult(null)
                }
                val streams = manager.episodeStreams(
                    matchedShowResults = showInfo,
                    request = requestInfo
                ).ifEmpty { null } ?: return@launch run {
                    Log.e("ShowService", "Empty Stream Results")
                    callback?.onResult(null)
                }
                Log.e("ShowService", "Found Streams: $streams")

                val result = Show.Response(
                    sources = streams
                )

                callback?.onResult(result.toByteArray())
            }
        }
    }
}