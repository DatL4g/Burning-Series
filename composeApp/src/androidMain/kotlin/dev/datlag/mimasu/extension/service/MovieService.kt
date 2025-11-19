package dev.datlag.mimasu.extension.service

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.datlag.mimasu.extension.IMovieProvider
import dev.datlag.mimasu.extension.movie.MovieCallback
import dev.datlag.mimasu.extension.movie.StreamCallback
import dev.datlag.mimasu.extension.provider.SearchManager
import dev.datlag.mimasu.extension.provider.model.Movie
import dev.datlag.tooling.safeCast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instanceOrNull

class MovieService : LifecycleService() {

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
    ) : IMovieProvider.Stub(), DIAware {

        private val _searchManager by instanceOrNull<SearchManager>()
        private val searchManager: SearchManager?
            get() = _searchManager ?: run {
                val newInstance by instanceOrNull<SearchManager>()
                newInstance
            }

        init {
            scope.launch(Dispatchers.IO) {
                searchManager?.initialize()
            }
        }

        override fun requestMovieId(request: ByteArray?, callback: MovieCallback?) {
            val manager = searchManager ?: return

            scope.launch(Dispatchers.IO) {
                val requestInfo = Movie.Request(request) ?: return@launch
                val id = manager.search(requestInfo) ?: return@launch
                callback?.onResult(id, true)
            }
        }

        override fun requestStream(movieId: Int, callback: StreamCallback?) {

        }

    }
}