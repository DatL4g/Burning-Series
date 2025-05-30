package dev.datlag.mimasu.extension.service

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.datlag.mimasu.extension.IMovieInfoProvider
import dev.datlag.mimasu.extension.movie.Callback
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

        Log.e("Extension Movie", "Bind Service")

        val di = applicationContext.safeCast<DIAware>()?.di
            ?: (application as? DIAware)?.di
            ?: return result

        return Binder(lifecycleScope, di)
    }

    class Binder(
        val scope: CoroutineScope,
        override val di: DI
    ) : IMovieInfoProvider.Stub(), DIAware {

        private val searchManager by instanceOrNull<SearchManager>()

        override fun requestInfo(request: ByteArray?, callback: Callback?) {
            val manager = searchManager ?: return

            scope.launch(Dispatchers.IO) {
                val requestInfo = Movie.Request(request)

                Log.e("Extension Movie", requestInfo?.tokenResult?.tokens?.joinToString() ?: "No RequestInfo")
            }
        }
    }
}