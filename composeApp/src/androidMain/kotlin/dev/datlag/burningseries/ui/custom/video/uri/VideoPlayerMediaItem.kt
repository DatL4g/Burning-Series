package dev.datlag.burningseries.ui.custom.video.uri

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.AssetDataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.RawResourceDataSource
import dev.datlag.tooling.async.scopeCatching

interface BaseVideoPlayerMediaItem {
    val mediaMetadata: MediaMetadata
    val mimeType: String?
}

/**
 * Representation of a media item for [io.sanghun.compose.video.VideoPlayer].
 *
 * @see RawResourceMediaItem
 * @see AssetFileMediaItem
 * @see StorageMediaItem
 * @see NetworkMediaItem
 */
sealed interface VideoPlayerMediaItem : BaseVideoPlayerMediaItem {

    /**
     * Converts [VideoPlayerMediaItem] to [android.net.Uri].
     *
     * @param context Pass application context or activity context. Use this context to load asset file using [android.content.res.AssetManager].
     * @return [android.net.Uri]
     */
    fun toUri(context: Context): Uri

    /**
     * A media item in the raw resource.
     * @param resourceId R.raw.xxxxx resource id
     * @param mediaMetadata Media Metadata. Default is empty.
     * @param mimeType Media mime type.
     */
    data class RawResourceMediaItem(
        @RawRes val resourceId: Int,
        override val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
        override val mimeType: String? = null,
    ) : VideoPlayerMediaItem {

        @OptIn(UnstableApi::class)
        override fun toUri(context: Context): Uri {
            return scopeCatching {
                Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .path(resourceId.toString())
                    .build()
            }.getOrNull() ?: RawResourceDataSource.buildRawResourceUri(resourceId)
        }
    }

    /**
     * A media item in the assets folder.
     * @param assetPath asset media file path (e.g If there is a test.mp4 file in the assets folder, test.mp4 becomes the assetPath.)
     * @throws androidx.media3.datasource.AssetDataSource.AssetDataSourceException asset file is not exist or load failed.
     * @param mediaMetadata Media Metadata. Default is empty.
     * @param mimeType Media mime type.
     */
    data class AssetFileMediaItem(
        val assetPath: String,
        override val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
        override val mimeType: String? = null,
    ) : VideoPlayerMediaItem {
        @OptIn(UnstableApi::class)
        override fun toUri(context: Context): Uri {
            val dataSpec = DataSpec(Uri.parse("asset:///$assetPath"))
            val assetDataSource = AssetDataSource(context)
            scopeCatching {
                assetDataSource.open(dataSpec)
            }

            return assetDataSource.uri ?: Uri.EMPTY
        }
    }

    /**
     * A media item in the device internal / external storage.
     * @param storageUri storage file uri
     * @param mediaMetadata Media Metadata. Default is empty.
     * @param mimeType Media mime type.
     * @throws androidx.media3.datasource.FileDataSource.FileDataSourceException
     */
    data class StorageMediaItem(
        val storageUri: Uri,
        override val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
        override val mimeType: String? = null,
    ) : VideoPlayerMediaItem {
        @OptIn(UnstableApi::class)
        override fun toUri(context: Context): Uri {
            val dataSpec = DataSpec(storageUri)
            val fileDataSource = FileDataSource()
            scopeCatching {
                fileDataSource.open(dataSpec)
            }

            return fileDataSource.uri ?: Uri.EMPTY
        }
    }

    /**
     * A media item in the internet
     * @param url network video url'
     * @param mediaMetadata Media Metadata. Default is empty.
     * @param mimeType Media mime type.
     * @param drmConfiguration Drm configuration for media. (Default is null)
     */
    data class NetworkMediaItem(
        val url: String,
        override val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
        override val mimeType: String? = null,
        val drmConfiguration: MediaItem.DrmConfiguration? = null,
    ) : VideoPlayerMediaItem {
        override fun toUri(context: Context): Uri {
            return Uri.parse(url)
        }
    }
}