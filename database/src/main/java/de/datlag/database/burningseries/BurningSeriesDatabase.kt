package de.datlag.database.burningseries

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.datlag.database.Converters
import de.datlag.model.burningseries.allseries.search.GenreItemFTS
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.series.*
import de.datlag.model.burningseries.series.relation.SeriesLanguagesCrossRef
import io.michaelrocks.paranoid.Obfuscate

@Database(
	entities = [
		LatestEpisode::class,
		LatestSeries::class,

		SeriesData::class,
		InfoData::class,
		SeasonData::class,
		LanguageData::class,
		EpisodeInfo::class,
		HosterData::class,

		GenreModel.GenreData::class,
		GenreModel.GenreItem::class,

		SeriesLanguagesCrossRef::class,

		GenreItemFTS::class
	],
	version = 5,
	exportSchema = true,
	autoMigrations = [
		AutoMigration(from = 1, to = 2),
		AutoMigration(from = 4, to = 5)
	]
)
@TypeConverters(Converters::class)
@Obfuscate
abstract class BurningSeriesDatabase : RoomDatabase() {
	
	abstract fun getBurningSeriesDao(): BurningSeriesDao
}