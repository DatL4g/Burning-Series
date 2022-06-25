package de.datlag.burningseries.module

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
    ).addCallback(object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL("INSERT INTO GenreItemFTS(GenreItemFTS) VALUES ('rebuild')")
        }
    }).fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideBurningSeriesDao(db: BurningSeriesDatabase) = db.getBurningSeriesDao()
}