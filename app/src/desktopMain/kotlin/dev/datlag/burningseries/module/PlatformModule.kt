package dev.datlag.burningseries.module

import dev.datlag.burningseries.datastore.CryptoManager
import org.kodein.di.*
import java.io.File

actual object PlatformModule {

    private const val NAME = "PlatformModuleDesktop"

    actual val di = DI.Module(NAME) {
        bindSingleton {
            CryptoManager()
        }

        bindProvider("UserSettingsFile") {
            val returnFile = File("/home/jeff/.config/BurningSeries/UserSettings.pb")
            if (!returnFile.exists()) {
                try {
                    returnFile.parentFile.mkdirs()
                } catch (ignored: Throwable) {}
                returnFile.createNewFile()
            }
            returnFile
        }
    }

}