package de.datlag.database.burningseries

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import de.datlag.database.Converters
import de.datlag.model.burningseries.Cover
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.allseries.search.GenreItemFTS
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestEpisodeInfoFlags
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.home.relation.LatestEpisodeCoverCrossRef
import de.datlag.model.burningseries.home.relation.LatestEpisodeInfoFlagsCrossRef
import de.datlag.model.burningseries.home.relation.LatestSeriesCoverCrossRef
import de.datlag.model.burningseries.series.*
import de.datlag.model.burningseries.series.relation.SeriesCoverCrossRef
import de.datlag.model.burningseries.series.relation.SeriesLanguagesCrossRef
import io.michaelrocks.paranoid.Obfuscate

@Database(
	entities = [
		Cover::class,

		LatestEpisode::class,
		LatestSeries::class,
		LatestEpisodeInfoFlags::class,

		SeriesData::class,
		InfoData::class,
		SeasonData::class,
		LanguageData::class,
		EpisodeInfo::class,
		HosterData::class,
		LinkedSeriesData::class,

		GenreModel.GenreData::class,
		GenreModel.GenreItem::class,

		SeriesLanguagesCrossRef::class,
		LatestEpisodeInfoFlagsCrossRef::class,
		LatestEpisodeCoverCrossRef::class,
		SeriesCoverCrossRef::class,
		LatestSeriesCoverCrossRef::class,

		GenreItemFTS::class
	],
	version = 7,
	autoMigrations = [
		AutoMigration(from = 1, to = 2),
		AutoMigration(from = 4, to = 5),
		AutoMigration(from = 5, to = 6)
	],
	exportSchema = true
)
@TypeConverters(Converters::class)
@Obfuscate
abstract class BurningSeriesDatabase : RoomDatabase() {
	
	abstract fun getBurningSeriesDao(): BurningSeriesDao

	@DeleteColumn(tableName = "SeriesTable", columnName = "image")
	class SeriesImageMigration : AutoMigrationSpec { }
}