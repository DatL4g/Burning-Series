package dev.datlag.burningseries.other

import androidx.compose.ui.awt.ComposeWindow
import com.sun.jna.platform.FileUtils
import dev.datlag.burningseries.common.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.harawata.appdirs.AppDirsFactory
import org.apache.commons.lang3.SystemUtils
import java.awt.Image
import java.io.File
import java.nio.file.Files
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

    private val dirs by lazy(LazyThreadSafetyMode.NONE) {
        AppDirsFactory.getInstance()
    }

    private val homeDir by lazy(LazyThreadSafetyMode.NONE) {
        systemProperty("user.home")?.let { File(it) }
    }

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

    fun getFileInConfigDir(name: String): File {
        var returnFile = File(dirs.getUserConfigDir("BurningSeries", null, null), name)
        if (returnFile.createWithParents() || returnFile.existsAndAccessible(true)) {
            return returnFile
        } else if (SystemUtils.IS_OS_LINUX) {
            val homeDir = this.homeDir ?: return returnFile
            val configDir = File(homeDir, ".config/BurningSeries")
            returnFile = File(configDir, name)
            returnFile.createWithParents()
            return returnFile
        }
        return returnFile
    }

    fun getFileInUserDataDir(name: String): File {
        var returnFile = File(dirs.getUserDataDir("BurningSeries", null, null), name)
        if (returnFile.createWithParents() || returnFile.existsAndAccessible(true)) {
            return returnFile
        } else if (SystemUtils.IS_OS_LINUX) {
            val homeDir = this.homeDir ?: return returnFile
            val dataDir = File(homeDir, ".local/share/BurningSeries")
            returnFile = File(dataDir, name)
            returnFile.createWithParents()
            return returnFile
        }
        return returnFile
    }

    fun getFolderInSiteDataDir(name: String): File {
        var returnFile = File(dirs.getSiteDataDir("BurningSeries", null, null), name)
        if (returnFile.existsOrCreateDirectory()) {
            return returnFile
        } else if (SystemUtils.IS_OS_LINUX) {
            val dataDir = File("/usr/local/share/BurningSeries", name)
            if (dataDir.existsOrCreateDirectory()) {
                return dataDir
            }

            val homeDir = this.homeDir ?: return returnFile
            returnFile = File(File(homeDir, ".local/share/flatpak/exports/share/BurningSeries"), name)
            returnFile.existsOrCreateDirectory()
            return returnFile
        }
        return returnFile
    }
}