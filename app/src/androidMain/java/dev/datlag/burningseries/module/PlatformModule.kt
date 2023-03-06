package dev.datlag.burningseries.module

import android.content.Context
import androidx.datastore.dataStoreFile
import dev.datlag.burningseries.database.DriverFactory
import dev.datlag.burningseries.datastore.CryptoManager
import dev.datlag.burningseries.network.video.Scraper
import dev.datlag.burningseries.network.video.VideoScraper
import org.kodein.di.*
import java.io.File

actual object PlatformModule {

    private const val NAME = "PlatformModuleAndroid"

    actual val di = DI.Module(NAME) {
        bindSingleton {
            CryptoManager()
        }

        bindSingleton("UserSettingsFile") {
            val app: Context = instance()
            app.dataStoreFile("UserSettings.pb")
        }

        bindSingleton("AppSettingsFile") {
            val app: Context = instance()
            app.dataStoreFile("AppSettings.pb")
        }

        bindSingleton {
            DriverFactory(instance()).createDriver()
        }

        bindSingleton("ImageDir") {
            val app: Context = instance()
            var returnFile = File(app.filesDir, "images")
            if (!returnFile.exists()) {
                try {
                    returnFile.mkdirs()
                } catch (ignored: Throwable) {
                    try {
                        returnFile.mkdir()
                    } catch (ignored: Throwable) {
                        returnFile = app.getDir("images", Context.MODE_PRIVATE) ?: returnFile
                    }
                }
            }
            returnFile
        }

        bindSingleton<Scraper> {
            VideoScraper
        }
    }

}