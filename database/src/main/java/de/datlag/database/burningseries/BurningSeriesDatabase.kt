package de.datlag.database.burningseries

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.datlag.database.Converters
import de.datlag.model.burningseries.series.SeriesData
import io.michaelrocks.paranoid.Obfuscate

@Database(
	entities = [
		SeriesData::class
	],
	version = 1
)
@TypeConverters(Converters::class)
@Obfuscate
abstract class BurningSeriesDatabase : RoomDatabase() {
	
	abstract fun getBurningSeriesDao(): BurningSeriesDao
}