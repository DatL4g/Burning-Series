package dev.datlag.burningseries.ui.custom.video.cache

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Manage video player cache.
 */
@UnstableApi
data object VideoPlayerCacheManager {

    private lateinit var cacheInstance: Cache

    /**
     * Set the cache for video player.
     * It can only be set once in the app, and it is shared and used by multiple video players.
     *
     * @param context Current activity context.
     * @param maxCacheBytes Sets the maximum cache capacity in bytes. If the cache builds up as much as the set capacity, it is deleted from the oldest cache.
     */
    fun initialize(context: Context, maxCacheBytes: Long) {
        if (::cacheInstance.isInitialized) {
            return
        }

        cacheInstance = SimpleCache(
            File(context.cacheDir, "video"),
            LeastRecentlyUsedCacheEvictor(maxCacheBytes),
            StandaloneDatabaseProvider(context)
        )
    }

    /**
     * Gets the ExoPlayer cache instance. If null, the cache to be disabled.
     */
    internal fun cache(): Cache? {
        return if (::cacheInstance.isInitialized) {
            cacheInstance
        } else {
            null
        }
    }
}