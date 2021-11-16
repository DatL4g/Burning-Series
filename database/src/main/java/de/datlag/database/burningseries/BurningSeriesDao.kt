package de.datlag.database.burningseries

import androidx.room.*
import de.datlag.model.burningseries.series.SeriesData
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.Flow

@Dao
@Obfuscate
interface BurningSeriesDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesData(seriesData: SeriesData): Long

    @Transaction
    @Delete
    suspend fun deleteSeriesData(seriesData: SeriesData)

    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE hrefTitle = :hrefTitle LIMIT 1")
    fun getSeriesByHrefTitle(hrefTitle: String): Flow<SeriesData>

    @Transaction
    @Query("SELECT * FROM SeriesTable WHERE isFavorite ORDER BY favoriteSince DESC")
    fun getSeriesFavorites(): Flow<List<SeriesData>>
}