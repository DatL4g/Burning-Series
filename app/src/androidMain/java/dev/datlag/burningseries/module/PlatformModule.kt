package dev.datlag.burningseries.module

import android.content.Context
import androidx.datastore.dataStoreFile
import dev.datlag.burningseries.datastore.CryptoManager
import org.kodein.di.*

actual object PlatformModule {

    private const val NAME = "PlatformModuleAndroid"

    actual val di = DI.Module(NAME) {
        bindSingleton {
            CryptoManager()
        }

        bindProvider("UserSettingsFile") {
            val app: Context = instance()
            app.dataStoreFile("UserSettings.pb")
        }
    }

}