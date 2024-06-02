package dev.datlag.burningseries.module

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import dev.datlag.burningseries.settings.DataStoreAppSettings
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.AppSettings
import dev.datlag.tooling.Tooling
import dev.datlag.tooling.createAsFileSafely
import dev.datlag.tooling.getRWUserConfigFile
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

actual object PlatformModule {

    private const val NAME = "DesktopPlatformModule"
    private const val APP_NAME = "Burning-Series"

    actual val di: DI.Module = DI.Module(NAME) {
        bindSingleton<DataStore<AppSettings>> {
            DataStoreFactory.create(
                storage = OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = AppSettings.SettingsSerializer,
                    producePath = {
                        Tooling.getRWUserConfigFile(
                            child = "app.settings",
                            appName = APP_NAME,
                            appVersion = "v6"
                        ).also { it.createAsFileSafely() }.toOkioPath()
                    }
                )
            )
        }
        bindSingleton<Settings.PlatformAppSettings> {
            DataStoreAppSettings(instance())
        }
    }
}