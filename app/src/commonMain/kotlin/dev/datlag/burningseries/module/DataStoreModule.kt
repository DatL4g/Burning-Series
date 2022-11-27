package dev.datlag.burningseries.module

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import dev.datlag.burningseries.datastore.UserSettingsSerializer
import dev.datlag.burningseries.datastore.preferences.UserSettings
import org.kodein.di.*

object DataStoreModule {

    private const val NAME = "DataStoreModule"

    val di = DI.Module(NAME) {
        import(PlatformModule.di)

        bindSingleton<DataStore<UserSettings>> {
            DataStoreFactory.create(
                UserSettingsSerializer(instance()),
                produceFile = { instance("UserSettingsFile") }
            )
        }
    }
}