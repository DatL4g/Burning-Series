package dev.datlag.burningseries.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import dev.datlag.burningseries.settings.DataStoreAppSettings
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.AppSettings
import dev.datlag.tooling.createAsFileSafely
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

actual object PlatformModule {

    private const val NAME = "AndroidPlatformModule"

    actual val di: DI.Module = DI.Module(NAME) {
        bindSingleton<DataStore<AppSettings>> {
            val app: Context = instance()

            DataStoreFactory.create(
                storage = OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = AppSettings.SettingsSerializer,
                    producePath = {
                        val path = app.filesDir.toOkioPath()
                            .resolve("v6")
                            .resolve("app.settings").also {
                                it.toFile().createAsFileSafely()
                            }

                        path
                    }
                )
            )
        }
        bindSingleton<Settings.PlatformAppSettings> {
            DataStoreAppSettings(instance())
        }
    }
}