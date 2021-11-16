package de.datlag.burningseries.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.datlag.database.burningseries.BurningSeriesDatabase
import de.datlag.model.Constants
import io.michaelrocks.paranoid.Obfuscate
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Obfuscate
object DatabaseModule {

    @Singleton
    @Provides
    fun provideBurningSeriesDatabase(
        @ApplicationContext app: Context
    ): BurningSeriesDatabase = Room.databaseBuilder(
        app,
        BurningSeriesDatabase::class.java,
        Constants.DATABASE_BURNING_SERIES
    ).build()

    @Singleton
    @Provides
    fun provideBurningSeriesDao(db: BurningSeriesDatabase) = db.getBurningSeriesDao()

}