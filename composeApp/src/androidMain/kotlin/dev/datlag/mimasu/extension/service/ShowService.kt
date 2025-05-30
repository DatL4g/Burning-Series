package dev.datlag.mimasu.extension.service

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.datlag.mimasu.extension.IShowInfoProvider
import dev.datlag.mimasu.extension.provider.SearchManager
import dev.datlag.mimasu.extension.provider.model.Show
import dev.datlag.mimasu.extension.show.Callback
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

        Log.e("Extension Show", "Bind Show Service")

        val di = applicationContext.safeCast<DIAware>()?.di
            ?: (application as? DIAware)?.di
            ?: return result

        return Binder(lifecycleScope, di)
    }

    class Binder(
        val scope: CoroutineScope,
        override val di: DI
    ) : IShowInfoProvider.Stub(), DIAware {

        private val searchManager by instanceOrNull<SearchManager>()

        override fun requestInfo(request: ByteArray?, callback: Callback?) {
            val manager = searchManager ?: run {
                val newInstance by instanceOrNull<SearchManager>()
                newInstance
            } ?: return

            scope.launch(Dispatchers.IO) {
                val requestInfo = Show.Request(request)

                Log.e("Extension Show", requestInfo?.tokenResult?.tokens?.joinToString() ?: "No show tokens")
            }
        }
    }
}