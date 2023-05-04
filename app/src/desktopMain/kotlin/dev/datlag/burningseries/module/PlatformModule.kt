package dev.datlag.burningseries.module

import dev.datlag.burningseries.common.createWithParents
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.database.DriverFactory
import dev.datlag.burningseries.datastore.CryptoManager
import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.model.HosterStream
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.network.video.Scraper
import dev.datlag.burningseries.other.AppIO
import dev.datlag.burningseries.scraper.video.VideoScraper
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

        bindSingleton {
            object : Scraper {
                override suspend fun scrapeVideosFrom(hosterStream: HosterStream): VideoStream? {
                    return VideoScraper.scrapeVideosFrom(hosterStream)
                }
            }
        }

        bindSingleton("ErrorFile") {
            AppIO.getFileInConfigDir("error.log")
        }

        bindSingleton("LoggingFile") {
            AppIO.getFileInConfigDir("logging.log")
        }
        bindSingleton {
            ActionLogger(instance("LoggingFile"))
        }
    }

}