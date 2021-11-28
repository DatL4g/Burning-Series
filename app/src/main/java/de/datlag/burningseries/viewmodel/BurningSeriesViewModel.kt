package de.datlag.burningseries.viewmodel

import androidx.lifecycle.*
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.home.HomeData
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.series.HosterData
import de.datlag.model.burningseries.series.SeriesData
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.model.jsonbase.BsHoster
import de.datlag.model.m3o.image.Convert
import de.datlag.network.burningseries.BurningSeriesRepository
import de.datlag.network.jsonbase.JsonBaseRepository
import de.datlag.network.m3o.M3ORepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class BurningSeriesViewModel @Inject constructor(
	val repository: BurningSeriesRepository,
	val jsonBase: JsonBaseRepository
): ViewModel() {
	
	val homeData = repository.getHomeData()
	val favorites = repository.getSeriesFavorites()
	val allSeries = repository.getAllSeries()

	fun getSeriesData(latestSeries: LatestSeries) = repository.getSeriesData(latestSeries)
	fun getSeriesData(latestEpisode: LatestEpisode) = repository.getSeriesData(latestEpisode)
	fun getSeriesData(genreItem: GenreModel.GenreItem) = repository.getSeriesData(genreItem)

	fun updateSeriesFavorite(seriesData: SeriesWithInfo) = viewModelScope.launch(Dispatchers.IO) {
		repository.updateSeriesFavorite(seriesData.series)
	}

	fun getBsHosterData(hoster: HosterData) = jsonBase.getBsHosterData(hoster).asLiveData(viewModelScope.coroutineContext)
}