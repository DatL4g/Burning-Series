package de.datlag.burningseries.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.datlag.datastore.SettingsPreferences
import de.datlag.datastore.serializer.SettingsSerializer
import io.michaelrocks.paranoid.Obfuscate
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Obfuscate
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext app: Context
    ): DataStore<SettingsPreferences> = DataStoreFactory.create(
        SettingsSerializer(),
        produceFile = { app.dataStoreFile("SettingsPreferences.pb") }
    )
}