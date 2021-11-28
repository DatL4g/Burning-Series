package de.datlag.database.burningseries

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.datlag.database.Converters
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.series.*
import de.datlag.model.burningseries.series.relation.SeriesLanguagesCrossRef
import io.michaelrocks.paranoid.Obfuscate

@Database(
	entities = [
		SeriesData::class,
		InfoData::class,
		SeasonData::class,
		LanguageData::class,
		EpisodeInfo::class,
		HosterData::class,

		GenreModel.GenreData::class,
		GenreModel.GenreItem::class,

		SeriesLanguagesCrossRef::class
	],
	version = 1
)
@TypeConverters(Converters::class)
@Obfuscate
abstract class BurningSeriesDatabase : RoomDatabase() {
	
	abstract fun getBurningSeriesDao(): BurningSeriesDao
}