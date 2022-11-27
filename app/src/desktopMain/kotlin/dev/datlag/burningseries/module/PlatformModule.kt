package dev.datlag.burningseries.module

import dev.datlag.burningseries.common.createWithParents
import dev.datlag.burningseries.datastore.CryptoManager
import net.harawata.appdirs.AppDirsFactory
import org.kodein.di.*
import java.io.File

actual object PlatformModule {

    private const val NAME = "PlatformModuleDesktop"

    actual val di = DI.Module(NAME) {
        bindSingleton {
            CryptoManager()
        }

        bindProvider("UserSettingsFile") {
            val dirs = AppDirsFactory.getInstance()
            val returnFile = File(dirs.getUserDataDir("BurningSeries", null, null), "UserSettings.pb")
            returnFile.createWithParents()
            returnFile
        }
    }

}