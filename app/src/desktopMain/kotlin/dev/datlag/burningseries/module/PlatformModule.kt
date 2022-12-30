package dev.datlag.burningseries.module

import dev.datlag.burningseries.common.createWithParents
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.database.DriverFactory
import dev.datlag.burningseries.datastore.CryptoManager
import net.harawata.appdirs.AppDirs
import net.harawata.appdirs.AppDirsFactory
import org.kodein.di.*
import java.io.File

actual object PlatformModule {

    private const val NAME = "PlatformModuleDesktop"

    actual val di = DI.Module(NAME) {
        bindSingleton {
            CryptoManager()
        }

        bindSingleton {
            AppDirsFactory.getInstance()
        }

        bindSingleton("UserSettingsFile") {
            val dirs: AppDirs = instance()
            val returnFile = File(dirs.getUserDataDir("BurningSeries", null, null), "UserSettings.pb")
            returnFile.createWithParents()
            returnFile
        }

        bindSingleton("AppSettingsFile") {
            val dirs: AppDirs = instance()
            val returnFile = File(dirs.getUserDataDir("BurningSeries", null, null), "AppSettings.pb")
            returnFile.createWithParents()
            returnFile
        }

        bindSingleton("DbFile") {
            val dirs: AppDirs = instance()
            val returnFile = File(dirs.getUserConfigDir("BurningSeries", null, null), "burning_series.db")
            returnFile.createWithParents()
            returnFile
        }

        bindSingleton {
            DriverFactory(instance("DbFile")).createDriver()
        }

        bindSingleton("ImageDir") {
            val dirs: AppDirs = instance()
            val returnFile = File(dirs.getSiteDataDir("BurningSeries", null, null), "images")
            if (!returnFile.exists()) {
                try {
                    returnFile.mkdirs()
                } catch (ignored: Throwable) {
                    try {
                        returnFile.mkdir()
                    } catch (ignored: Throwable) { }
                }
            }
            returnFile
        }
    }

}