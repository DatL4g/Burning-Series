package dev.datlag.burningseries.other

import androidx.compose.ui.awt.ComposeWindow
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.common.mapAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Image
import javax.swing.ImageIcon

object AppIO {

    private val appIcons = listOf(
        "launcher_128",
        "launcher_96",
        "launcher_64",
        "launcher_48",
        "launcher_32",
        "launcher_16",
    ).flatMap {
        listOf("png/$it.png", "svg/$it.svg", "ico/$it.ico")
    }.toMutableList().apply {
        add("icns/launcher.icns")
    }.toList()

    fun loadAppIcon(
        window: ComposeWindow,
        resources: Resources,
        scope: CoroutineScope
    ) = scope.launch(Dispatchers.IO) {
        val appIcons = getAppImages(resources, this@AppIO.appIcons)

        withContext(CommonDispatcher.Main) {
            window.iconImages = appIcons
        }
    }

    private suspend fun getAppImages(resources: Resources, list: Collection<String>): List<Image> {
        return list.mapAsync { getAppImage(resources, it) }.filterNotNull()
    }

    private suspend fun getAppImage(resources: Resources, location: String): Image? = runCatching {
        resources.getResourcesAsInputStream(location)?.use { ImageIcon(it.readBytes()).image }
    }.getOrNull()

}