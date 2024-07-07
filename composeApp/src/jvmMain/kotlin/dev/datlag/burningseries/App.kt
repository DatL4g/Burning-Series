package dev.datlag.burningseries

import dev.datlag.tooling.Tooling
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.compose.getResourcesAsInputStream
import dev.datlag.tooling.compose.withIOContext
import dev.icerock.moko.resources.AssetResource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.awt.Image
import javax.swing.ImageIcon

data object App {
    suspend fun loadAppIcon(
        vararg assets: AssetResource
    ) = withIOContext {
        return@withIOContext assets.map {
            async {
                getAppImage(it)
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun getAppImage(asset: AssetResource): Image? = suspendCatching {
        (Tooling.getResourcesAsInputStream(App::class, asset.filePath)
            ?: Tooling.getResourcesAsInputStream(App::class, asset.originalPath)
        )?.use {
            ImageIcon(it.readBytes()).image
        }
    }.getOrNull()
}