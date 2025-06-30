package dev.datlag.mimasu.extension.service

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.BuildConfig
import dev.datlag.mimasu.extension.IUpdateProvider
import dev.datlag.mimasu.extension.github.GitHub
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.mimasu.extension.update.Callback
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.safeCast
import io.github.z4kn4fein.semver.toVersionOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instanceOrNull
import kotlin.ByteArray
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

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

        fun hasDownloadUrl() = !_downloadUrl?.ifBlank { null }.isNullOrBlank()

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
        private val updateKache = InMemoryKache<UpdateCacheKey, Update>(
            maxSize = 2L * 1024 * 1024
        ) {
            strategy = KacheStrategy.LRU
            expireAfterWriteDuration = 12.hours
        }

        private suspend fun getUpdate(git: GitHub): Update? {
            val key = UpdateCacheKey(
                owner = "DatL4g",
                repository = "Mimasu-Extension"
            )

            return updateKache.async(key) {
                suspendCatching {
                    git.latestRelease(
                        owner = key.owner,
                        repo = key.repository
                    )
                }.getOrNull()?.let { release ->
                    val releaseVersion = release.tagName.toVersionOrNull(strict = false)
                    val appVersion = BuildConfig.VERSION_NAME.toVersionOrNull(strict = false)

                    if (releaseVersion == null || appVersion == null) {
                        null
                    } else {
                        if (releaseVersion > appVersion) {
                            Update(
                                _available = !release.draft,
                                _viewUrl = release.htmlUrl.ifBlank { null },
                                _downloadUrl = release.assets.maxByOrNull {
                                    it.apkIdentifier
                                }?.takeIf {
                                    it.hasAnyApkIdentifier
                                }?.downloadUrl?.toString()?.ifBlank { null }
                            )
                        } else {
                            null
                        }
                    }
                }
            }
        }

        override fun requestUpdate(callback: Callback?) {
            val git = github ?: return

            scope.launch(Dispatchers.IO) {
                val availableUpdate = suspendCatching {
                    getUpdate(git)?.toByteArray()
                }.getOrNull() ?: return@launch

                withContext(Dispatchers.Main) {
                    callback?.onResult(availableUpdate)
                }
            }
        }

        data class UpdateCacheKey(
            val owner: String,
            val repository: String
        )
    }
}