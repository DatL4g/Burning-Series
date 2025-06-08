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
            val manager = searchManager ?: return

            scope.launch(Dispatchers.IO) {
                val requestInfo = Show.Request(request) ?: return@launch
                val id = manager.search(requestInfo) ?: return@launch
                callback?.onResult(id)
            }
        }

        override fun requestEpisodeAvailability(
            showId: Int,
            request: ByteArray?,
            callback: EpisodeCallback?
        ) {
            val showHolder = searchManager ?: return
            val manager = episodeManager ?: return

            scope.launch(Dispatchers.IO) {
                val requestInfo = Show.EpisodeRequest(request) ?: return@launch
                val showInfo = showHolder.matchedShowResults(showId) ?: return@launch
                val available = manager.episodeAvailability(
                    showId = showId,
                    matchedShowResults = showInfo,
                    request = requestInfo
                )

                callback?.onResult(available)
            }
        }

        override fun requestStream(showId: Int, request: ByteArray?, callback: StreamCallback?) {
            val showHolder = searchManager ?: return
            val manager = episodeManager ?: return

            scope.launch(Dispatchers.IO) {
                val requestInfo = Show.EpisodeRequest(request) ?: return@launch
                val showInfo = showHolder.matchedShowResults(showId) ?: return@launch
                val streams = manager.episodeStreams(
                    matchedShowResults = showInfo,
                    request = requestInfo
                ).toSet().ifEmpty { null } ?: return@launch
                val result = Show.Response(
                    sources = mapOf("de" to streams.toList())
                )

                callback?.onResult(result.toByteArray())
            }
        }
    }
}