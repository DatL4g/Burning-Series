package dev.datlag.burningseries

import dev.datlag.burningseries.model.common.*
import net.harawata.appdirs.AppDirsFactory
import org.apache.commons.lang3.SystemUtils
import java.awt.Toolkit
import java.io.File

object AppIO {

    private val dirs by lazy {
        AppDirsFactory.getInstance()
    }

    fun applyTitle(title: String) = scopeCatching {
        val toolkit = Toolkit.getDefaultToolkit()
        val awtAppClassNameField = toolkit.javaClass.getDeclaredField("awtAppClassName")
        val working = try {
            awtAppClassNameField.isAccessible = true
            awtAppClassNameField.canAccess(null)
        } catch (ignored: Throwable) {
            awtAppClassNameField.trySetAccessible()
        }
        awtAppClassNameField.set(toolkit, title)
        working
    }.getOrNull() ?: false

    fun getFileInSiteDataDir(name: String): File {
        val parentFile = File(dirs.getSiteDataDir(APP_NAME, null, null))
        var returnFile = File(parentFile, name)
        if (returnFile.existsRWSafely()
            || (returnFile.parentFile ?: parentFile).existsRWSafely()
            || (returnFile.parentFile ?: parentFile).mkdirsSafely()) {
            return returnFile
        } else if (SystemUtils.IS_OS_LINUX) {
            val dataDir = File("/usr/local/share/$APP_NAME")
            returnFile = File(dataDir, name)
            if (returnFile.existsRWSafely()
                || (returnFile.parentFile ?: dataDir).existsRWSafely()
                || (returnFile.parentFile ?: dataDir).mkdirsSafely()) {
                return returnFile
            }

            val alternativeDataDir = File(homeDirectory(), ".local/share/flatpak/exports/share/$APP_NAME").apply {
                mkdirsSafely()
            }
            returnFile = File(alternativeDataDir, name)
            return returnFile
        }
        return returnFile
    }

    fun getWriteableExecutableFolder(): File {
        val resDir = systemProperty("compose.application.resources.dir")?.let { File(it) }
        return if (resDir.existsRWSafely()) {
            resDir!!
        } else {
            if (File("./").canWriteSafely()) {
                File("./")
            } else {
                getFileInSiteDataDir("./")
            }
        }
    }

    private const val APP_NAME = "Burning-Series"
}