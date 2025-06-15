package dev.datlag.mimasu.extension.service

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.datlag.mimasu.extension.IUpdateProvider
import dev.datlag.mimasu.extension.github.GitHub
import dev.datlag.mimasu.extension.update.Callback
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.safeCast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instanceOrNull
import kotlin.ByteArray

class UpdateService : LifecycleService() {

    override fun onBind(intent: Intent): IBinder? {
        val result = super.onBind(intent)

        val di = applicationContext.safeCast<DIAware>()?.di
            ?: (application as? DIAware)?.di
            ?: return result

        return Binder(lifecycleScope, di)
    }

    @Serializable
    data class Update(
        private val _available: Boolean? = null,
        private val _downloadUrl: String? = null,
        private val _viewUrl: String? = null
    ) {

        @OptIn(ExperimentalSerializationApi::class)
        fun toByteArray(): ByteArray {
            return protobuf.encodeToByteArray(this)
        }

        companion object {
            @OptIn(ExperimentalSerializationApi::class)
            private val protobuf = ProtoBuf {
                encodeDefaults = false
            }
        }
    }

    class Binder(
        val scope: CoroutineScope,
        override val di: DI
    ) : IUpdateProvider.Stub(), DIAware {

        private val github by instanceOrNull<GitHub>()

        override fun requestUpdate(callback: Callback?) {
            val git = github ?: return

            callback?.onResult(Update(
                _available = true,
                _viewUrl = "https://datlag.dev"
            ).toByteArray())

            // Cache, mutex and check app version
            /*scope.launch(Dispatchers.IO) {
                val release = suspendCatching {
                    git.latestRelease(
                        owner = "DatL4g",
                        repo = "Burning-Series"
                    )
                }.onSuccess {
                    Log.e("UpdateService", "Got: $it")
                }.onFailure {
                    Log.e("UpdateService", "Failed", it)
                }.getOrNull()

                val info = Update(
                    _available = release != null,
                    _viewUrl = release?.htmlUrl?.ifBlank { null },
                    _downloadUrl = release?.assets?.maxByOrNull {
                        it.apkIdentifier
                    }?.takeIf {
                        it.hasAnyApkIdentifier
                    }?.downloadUrl?.toString()?.ifBlank { null }
                ).toByteArray()

                withContext(Dispatchers.Main) {
                    callback?.onResult(info)
                }
            }*/
        }
    }
}