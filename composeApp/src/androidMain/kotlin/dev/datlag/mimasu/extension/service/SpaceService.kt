package dev.datlag.mimasu.extension.service

import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.os.storage.StorageManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.datlag.mimasu.extension.ISpaceProvider
import dev.datlag.mimasu.extension.common.deleteRecursivelySafely
import dev.datlag.mimasu.extension.provider.EpisodeManager
import dev.datlag.mimasu.extension.provider.SearchManager
import dev.datlag.mimasu.extension.space.SpaceCallback
import dev.datlag.tooling.alsoFalse
import dev.datlag.tooling.alsoTrue
import dev.datlag.tooling.async.launchVirtualIO
import dev.datlag.tooling.safeCast
import dev.datlag.tooling.scopeCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instanceOrNull
import java.security.MessageDigest
import kotlin.getValue

class SpaceService : LifecycleService() {

    override fun onBind(intent: Intent): IBinder? {
        val result = super.onBind(intent)

        val di = applicationContext.safeCast<DIAware>()?.di
            ?: (application as? DIAware)?.di
            ?: return result

        return Binder(lifecycleScope, di, this)
    }

    class Binder(
        val scope: CoroutineScope,
        override val di: DI,
        private val context: Context
    ) : ISpaceProvider.Stub(), DIAware {

        private val allowedSignatures = listOf(
            "23:2c:6b:16:a7:9c:48:1e:6f:42:cb:3a:38:9c:c9:a0:92:44:66:e6:e7:7d:b1:12:3d:d9:94:ca:93:44:4c:1f",
            "81:c8:0f:f7:2f:bb:45:01:de:d6:52:37:27:4b:cb:13:ed:fe:73:84:9f:c4:1a:1d:e5:ff:c5:f0:bd:da:e9:f8"
        )

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

        private val storageStatsManager = context.getSystemService<StorageStatsManager>() ?: scopeCatching {
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        }.getOrNull() ?: ContextCompat.getSystemService(context, StorageStatsManager::class.java)

        private val storageManager = context.getSystemService<StorageManager>() ?: scopeCatching {
            context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        }.getOrNull() ?: ContextCompat.getSystemService(context, StorageManager::class.java)

        private val activityManager = context.getSystemService<ActivityManager>() ?: scopeCatching {
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        }.getOrNull() ?: ContextCompat.getSystemService(context, ActivityManager::class.java)

        override fun clearCache(callback: SpaceCallback?): Boolean {
            return isSignatureAllowed().alsoTrue {
                context.cacheDir.deleteRecursivelySafely()
                scope.launchVirtualIO {
                    episodeManager?.clear()
                    searchManager?.clear()

                    val sizes = loadSizes()
                    callback?.onResult(sizes.app, sizes.userData, sizes.cache)
                }
            }.alsoFalse {
                updateSizes(callback)
            }
        }

        override fun clearStorage(callback: SpaceCallback?): Boolean {
            return isSignatureAllowed().alsoTrue {
                activityManager?.clearApplicationUserData()?.let {
                    it && clearCache(callback)
                } ?: clearCache(callback)
            }.alsoFalse {
                updateSizes(callback)
            }
        }

        override fun requestSpace(callback: SpaceCallback?) {
            updateSizes(callback)
        }

        private fun isSignatureAllowed(): Boolean {
            fun hashSignature(signature: Signature): String? = scopeCatching {
                val md = MessageDigest.getInstance("SHA-256")
                md.update(signature.toByteArray())
                md.digest().joinToString(separator = ":") { "%02x".format(it) }
            }.getOrNull()

            fun check(packageName: String): Boolean {
                val signatures = scopeCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val flags = PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
                        val packageInfo = context.packageManager.getPackageInfo(packageName, flags)
                        packageInfo.signingInfo?.apkContentsSigners ?: packageInfo.signatures
                    } else {
                        val flags = PackageManager.GET_SIGNATURES
                        val packageInfo = context.packageManager.getPackageInfo(packageName, flags)
                        packageInfo.signatures
                    }
                }.getOrNull()?.mapNotNull {
                    hashSignature(it ?: return@mapNotNull null)
                }?.ifEmpty { null } ?: return false

                return signatures.any { s ->
                    allowedSignatures.any { a ->
                        s.equals(a, ignoreCase = true)
                    }
                }
            }

            val callingUid = scopeCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    getCallingUidOrThrow()
                } else null
            }.getOrNull() ?: scopeCatching {
                getCallingUid()
            }.getOrNull() ?: return false

            val packages = context.packageManager.getPackagesForUid(callingUid)?.ifEmpty { null } ?: return false

            return packages.any { p ->
                check(p)
            }
        }

        private fun updateSizes(callback: SpaceCallback?) {
            scope.launchVirtualIO {
                val sizes = loadSizes()
                callback?.onResult(sizes.app, sizes.userData, sizes.cache)
            }
        }

        private suspend fun loadSizes(): Sizes {
            return modernSizes() ?: legacySizes() ?: Sizes(0, 0, 0)
        }

        private fun modernSizes(): Sizes? {
            val appSpecificStorageUuid = scopeCatching {
                storageManager?.getUuidForPath(context.filesDir)
            }.getOrNull() ?: return null
            val user = Process.myUserHandle()

            return scopeCatching {
                val storageStats = storageStatsManager?.queryStatsForPackage(
                    appSpecificStorageUuid,
                    context.packageName,
                    user
                ) ?: return@scopeCatching null

                Sizes(
                    _app = storageStats.appBytes,
                    _userData = storageStats.dataBytes,
                    _cache = storageStats.cacheBytes
                ).takeUnless { it.isEmpty() }
            }.getOrNull()
        }

        private suspend fun legacySizes(): Sizes? = coroutineScope {
            val userData = async {
                context.filesDir.walkTopDown().sumOf {
                    scopeCatching {
                        it.length()
                    }.getOrNull() ?: 0
                }
            }
            val cache = async {
                context.cacheDir.walkTopDown().sumOf {
                    scopeCatching {
                        it.length()
                    }.getOrNull() ?: 0
                }
            }

            return@coroutineScope Sizes(
                _app = 0,
                _userData = userData.await(),
                _cache = cache.await()
            ).takeUnless { it.isEmpty() }
        }

        @Serializable
        data class Sizes(
            private val _app: Long,
            private val _userData: Long,
            private val _cache: Long
        ) {
            val app: Long = _app.takeIf { it > 0 } ?: 0
            val userData: Long = _userData.takeIf { it > 0 } ?: 0
            val cache: Long = _cache.takeIf { it > 0 } ?: 0
            val total: Long = app + userData + cache

            fun isEmpty(): Boolean {
                return total <= 0
            }
        }
    }
}