package de.datlag.database.burningseries

import androidx.room.*
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.allseries.relation.GenreWithItems
import de.datlag.model.burningseries.allseries.search.GenreItemWithMatchInfo
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestEpisodeInfoFlags
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.home.relation.LatestEpisodeInfoFlagsCrossRef
import de.datlag.model.burningseries.home.relation.LatestEpisodeWithInfoFlags
import de.datlag.model.burningseries.series.*
import de.datlag.model.burningseries.series.relation.EpisodeWithHoster
import de.datlag.model.burningseries.series.relation.SeriesLanguagesCrossRef
import de.datlag.model.burningseries.series.relation.SeriesWithEpisode
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.*

@Dao
@Obfuscate
interface BurningSeriesDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLatestEpisode(latestEpisode: LatestEpisode): Long

    @Transaction
    @Delete
    suspend fun deleteLatestEpisode(latestEpisode: LatestEpisode)

    @Transaction
    @Query("DELETE FROM LatestEpisodeTable")
    suspend fun deleteAllLatestEpisode()

    @Transaction
    @Query("SELECT * FROM LatestEpisodeTable")
    fun getAllLatestEpisode(): Flow<List<LatestEpisodeWithInfoFlags>>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLatestEpisodeInfoFlags(latestEpisodeInfoFlags: LatestEpisodeInfoFlags): Long

    suspend fun addLatestEpisodeInfoFlags(latestEpisodeInfoFlags: LatestEpisodeInfoFlags): Long {
        if (latestEpisodeInfoFlags.latestEpisodeInfoFlagsId > 0L) {
            return latestEpisodeInfoFlags.latestEpisodeInfoFlagsId
        }
        val valueFlags = getLatestEpisodeInfoFlagsByClass(latestEpisodeInfoFlags.classNames).firstOrNull()
        if (valueFlags != null && valueFlags.latestEpisodeInfoFlagsId > 0L) {
            return valueFlags.latestEpisodeInfoFlagsId
        }
        return insertLatestEpisodeInfoFlags(latestEpisodeInfoFlags)
    }

    @Transaction
    @Delete
    suspend fun deleteLatestEpisodeInfoFlags(latestEpisodeInfoFlags: LatestEpisodeInfoFlags)

    @Transaction
    @Query("SELECT * FROM LatestEpisodeInfoFlagsTable WHERE classNames = :classNames LIMIT 1")
    fun getLatestEpisodeInfoFlagsByClass(classNames: String): Flow<LatestEpisodeInfoFlags>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLatestEpisodeInfoFlagsCrossRef(latestEpisodeInfoFlagsCrossRef: LatestEpisodeInfoFlagsCrossRef)

    @Transaction
    @Delete
    suspend fun deleteLatestEpisodeInfoFlagsCrossRef(latestEpisodeInfoFlagsCrossRef: LatestEpisodeInfoFlagsCrossRef)



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLatestSeries(latestSeries: LatestSeries): Long

    @Transaction
    @Delete
    suspend fun deleteLatestSeries(latestSeries: LatestSeries)

    @Transaction
    @Query("DELETE FROM LatestSeriesTable")
    suspend fun deleteAllLatestSeries()

    @Transaction
    @Query("SELECT * FROM LatestSeriesTable")
    fun getAllLatestSeries(): Flow<List<LatestSeries>>



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesData(seriesData: SeriesData): Long

    @Transaction
    @Query("UPDATE SeriesTable SET favoriteSince = :favSeconds WHERE seriesId = :id")
    suspend fun updateSeriesFavorite(id: Long, favSeconds: Long): Int

    @Transaction
    @Delete
    suspend fun deleteSeriesData(seriesData: SeriesData)

    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE seriesId = :id LIMIT 1")
    fun getSeriesWithInfoById(id: Long): Flow<SeriesWithInfo?>

    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE hrefTitle = :hrefTitle LIMIT 1")
    fun getSeriesByHrefTitle(hrefTitle: String): Flow<SeriesData>

    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE favoriteSince > 0 ORDER BY favoriteSince DESC")
    fun getSeriesFavorites(): Flow<List<SeriesWithInfo>>

    @Transaction
    @Query("SELECT favoriteSince FROM SeriesTable WHERE hrefTitle = :hrefTitle OR hrefTitle LIKE :hrefTitle || '%' LIMIT 1")
    fun getSeriesFavoriteSinceByHrefTitle(hrefTitle: String): Flow<Long?>

    @Transaction
    @Query(
        "SELECT DISTINCT SeriesTable.* FROM SeriesTable " +
            "INNER JOIN EpisodeInfoTable ON SeriesTable.seriesId = EpisodeInfoTable.seriesId " +
            "WHERE SeriesTable.favoriteSince > 0 OR EpisodeInfoTable.currentWatchPos > 0 OR EpisodeInfoTable.totalWatchPos > 0"
    )
    fun getSeriesFavoritesAndWatched(): Flow<List<SeriesWithInfo>>



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfoData(infoData: InfoData): Long

    @Transaction
    @Delete
    suspend fun deleteInfoData(infoData: InfoData)

    @Transaction
    @Query("SELECT * FROM InfoTable")
    fun getInfoData(): List<InfoData>

    @Transaction
    @Query("SELECT * FROM InfoTable WHERE seriesId = :seriesId")
    fun getInfoDataBySeriesId(seriesId: Long): Flow<List<InfoData>>



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasonData(seasonData: SeasonData): Long

    @Transaction
    @Delete
    suspend fun deleteSeasonData(seasonData: SeasonData)

    @Transaction
    @Query("SELECT * FROM SeasonTable")
    fun getSeasonData(): List<SeasonData>



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguageData(languageData: LanguageData): Long

    suspend fun addLanguageData(languageData: LanguageData): Long {
        if (languageData.languageId > 0L) {
            return languageData.languageId
        }
        val valueLang = getLanguageDataByValue(languageData.value).firstOrNull()
        if (valueLang != null && valueLang.languageId > 0L) {
            return valueLang.languageId
        }
        return insertLanguageData(languageData)
    }

    @Transaction
    @Delete
    suspend fun deleteLanguageData(languageData: LanguageData)

    @Transaction
    @Query("SELECT * FROM LanguageTable")
    fun getLanguageData(): List<LanguageData>

    @Transaction
    @Query("SELECT * FROM LanguageTable WHERE value = :value LIMIT 1")
    fun getLanguageDataByValue(value: String): Flow<LanguageData?>



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesLanguagesCrossRef(seriesLanguagesCrossRef: SeriesLanguagesCrossRef)

    @Transaction
    @Delete
    suspend fun deleteSeriesLanguagesCrossRef(seriesLanguagesCrossRef: SeriesLanguagesCrossRef)



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodeInfo(episodeInfo: EpisodeInfo): Long

    @Transaction
    @Delete
    suspend fun deleteEpisodeInfo(episodeInfo: EpisodeInfo)

    @Transaction
    @Update
    fun updateEpisodeInfo(episodeInfo: EpisodeInfo)

    @Transaction
    @Query("SELECT currentWatchPos FROM EpisodeInfoTable WHERE href = :href LIMIT 1")
    fun getEpisodeCurrentWatchPosByHref(href: String): Long?

    @Transaction
    @Query("SELECT totalWatchPos FROM EpisodeInfoTable WHERE href = :href LIMIT 1")
    fun getEpisodeTotalWatchPosByHref(href: String): Long?

    @Transaction
    @Query("SELECT * FROM EpisodeInfoTable WHERE episodeId = :id OR href = :href LIMIT 1")
    fun getEpisodeInfoByIdOrHref(id: Long, href: String): Flow<EpisodeInfo?>



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoster(hosterData: HosterData): Long

    @Transaction
    @Delete
    suspend fun deleteHoster(hosterData: HosterData)

    @Transaction
    @Query("SELECT * FROM EpisodeInfoTable")
    fun getEpisodeWithHoster(): Flow<List<EpisodeWithHoster>>

    @Transaction
    @Query("SELECT * FROM EpisodeInfoTable WHERE seriesId = :seriesId AND number = :number LIMIT 1")
    fun getEpisodeInfoBySeriesAndNumber(seriesId: Long, number: String): Flow<EpisodeWithHoster?>



    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE hrefTitle = :hrefTitle LIMIT 1")
    fun getSeriesWithInfoByHrefTitle(hrefTitle: String): Flow<SeriesWithInfo?>

    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE hrefTitle = :hrefTitle LIMIT 1")
    fun getSeriesWithEpisodeByHrefTitle(hrefTitle: String): Flow<SeriesWithEpisode?>

    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE :hrefTitle LIKE hrefTitle || '%' OR hrefTitle LIKE :hrefTitle || '%' LIMIT 1")
    fun getSeriesWithInfoByLikeHrefTitle(hrefTitle: String): Flow<SeriesWithInfo?>

    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE :hrefTitle LIKE hrefTitle || '%' OR hrefTitle LIKE :hrefTitle || '%' LIMIT 1")
    fun getSeriesWithEpisodeByLikeHrefTitle(hrefTitle: String): Flow<SeriesWithEpisode?>

    fun getSeriesWithInfoBestMatch(hrefTitle: String): Flow<SeriesWithInfo?> = flow {
        getSeriesWithInfoByHrefTitle(hrefTitle).collect {
            if (it != null) {
                emit(it)
            } else {
                emitAll(getSeriesWithInfoByLikeHrefTitle(hrefTitle))
            }
        }
    }

    fun getSeriesWithEpisodesBestMatch(hrefTitle: String): Flow<SeriesWithEpisode?> = flow {
        getSeriesWithEpisodeByHrefTitle(hrefTitle).collect {
            if (it != null) {
                emit(it)
            } else {
                emitAll(getSeriesWithEpisodeByLikeHrefTitle(hrefTitle))
            }
        }
    }


    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE favoriteSince > 0 AND title LIKE '%' || :title || '%' ORDER BY favoriteSince DESC")
    fun searchFavorites(title: String): Flow<List<SeriesWithInfo>>



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenre(genreData: GenreModel.GenreData): Long

    @Transaction
    @Delete
    suspend fun deleteGenre(genreData: GenreModel.GenreData)

    @Transaction
    @Query("SELECT * FROM GenreTable ORDER BY GenreTable.genre")
    fun getAllGenres(): Flow<List<GenreModel.GenreData>>



    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenreItem(genreItem: GenreModel.GenreItem): Long

    @Transaction
    @Delete
    suspend fun deleteGenreItem(genreItem: GenreModel.GenreItem)



    @Transaction
    @Query("SELECT DISTINCT COUNT(*) FROM GenreTable INNER JOIN GenreItemTable ON GenreTable.genreId = GenreItemTable.genreId")
    fun getAllSeriesCountJoined(): Flow<Long>



    @Transaction
    @Query("SELECT * FROM GenreTable ORDER BY GenreTable.genre LIMIT 1 OFFSET :offset")
    fun getAllSeries(offset: Long = 0L): Flow<List<GenreWithItems>>

    @Transaction
    @Query("SELECT COUNT(genreId) FROM GenreTable")
    fun getAllSeriesCount(): Flow<Long>

    @Transaction
    @Query("SELECT DISTINCT * FROM (SELECT item.title as title, item.href as href, item.genreId as genreId, item.genreItemId as genreItemId, matchinfo(GenreItemFTS) as matchInfo FROM GenreItemTable AS item " +
            "INNER JOIN GenreItemFTS AS fts ON item.href = fts.href WHERE GenreItemFTS MATCH '*' || :matchQuery || '*'" +
            "UNION ALL SELECT *, NULL as matchInfo FROM GenreItemTable WHERE title LIKE '%' || :matchQuery ||'%' OR title LIKE '%' || :likeQuery || '%') GROUP BY genreItemId")
    fun searchAllSeries(matchQuery: String, likeQuery: String): Flow<List<GenreItemWithMatchInfo>>
}