package dev.datlag.burningseries.module

import dev.datlag.burningseries.common.createWithParents
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.database.DriverFactory
import dev.datlag.burningseries.datastore.CryptoManager
import dev.datlag.burningseries.network.video.Scraper
import dev.datlag.burningseries.network.video.VideoScraper
import dev.datlag.burningseries.other.AppIO
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

        bindSingleton("UserSettingsFile") {
            AppIO.getFileInUserDataDir("UserSettings.pb")
        }

        bindSingleton("AppSettingsFile") {
            AppIO.getFileInUserDataDir("AppSettings.pb")
        }

        bindSingleton("DbFile") {
            AppIO.getFileInConfigDir("burning_series.db")
        }

        bindSingleton {
            DriverFactory(instance("DbFile")).createDriver()
        }

        bindSingleton("ImageDir") {
            AppIO.getFolderInSiteDataDir("images")
        }

        bindSingleton<Scraper> {
            VideoScraper
        }
    }

}