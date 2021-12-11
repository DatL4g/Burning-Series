package de.datlag.burningseries.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.allseries.relation.GenreWithItems
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.series.HosterData
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.network.burningseries.BurningSeriesRepository
import de.datlag.network.m3o.M3ORepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class BurningSeriesViewModel @Inject constructor(
	val repository: BurningSeriesRepository,
	val m3ORepository: M3ORepository
): ViewModel() {

	var showedHelpImprove: Boolean = false

	val homeData = repository.getHomeData()
	val favorites: MutableSharedFlow<List<SeriesWithInfo>> = MutableSharedFlow()

	val allSeriesPagination: MutableStateFlow<Long> = MutableStateFlow(0)
	val allSeriesPaginated: MutableSharedFlow<Resource<List<GenreWithItems>>> = MutableSharedFlow()
	val allSeriesPaginatedFlat: MutableSharedFlow<List<GenreModel>> = MutableSharedFlow()

	init {
		getAllFavorites()
		viewModelScope.launch(Dispatchers.IO) {
			allSeriesPaginated.collect {
				it.data?.flatMap { item -> item.toGenreModel() }?.let { items -> allSeriesPaginatedFlat.emit(items) }
			}
		}
	}

	fun getAllSeriesNext() = viewModelScope.launch(Dispatchers.IO) {
		val maxValue = repository.getAllSeriesCount().first()
		if (allSeriesPagination.value + 1 < maxValue) {
			allSeriesPagination.emit(allSeriesPagination.value + 1)
		} else {
			allSeriesPagination.emit(0)
		}
	}

	fun getAllSeriesPrevious() = viewModelScope.launch(Dispatchers.IO) {
		val maxValue = repository.getAllSeriesCount().first()
		if (allSeriesPagination.value - 1 < 0) {
			allSeriesPagination.emit(maxValue - 1)
		} else {
			allSeriesPagination.emit(allSeriesPagination.value -1)
		}
	}

	fun getNewPaginationData() = viewModelScope.launch(Dispatchers.IO) {
		allSeriesPaginated.emitAll(repository.getAllSeries(allSeriesPagination.value))
	}

	fun searchAllSeries(title: String) = viewModelScope.launch(Dispatchers.IO) {
		allSeriesPaginatedFlat.emitAll(repository.searchAllSeries(title))
	}

	fun getAllFavorites() = viewModelScope.launch(Dispatchers.IO) {
		favorites.emitAll(repository.getSeriesFavorites())
	}

	fun searchFavorites(title: String) = viewModelScope.launch(Dispatchers.IO) {
		favorites.emitAll(repository.searchSeriesFavorites(title))
	}

	fun getSeriesData(latestSeries: LatestSeries) = repository.getSeriesData(latestSeries)
	fun getSeriesData(latestEpisode: LatestEpisode) = repository.getSeriesData(latestEpisode)
	fun getSeriesData(genreItem: GenreModel.GenreItem) = repository.getSeriesData(genreItem)
	fun getSeriesData(href: String, hrefTitle: String, forceLoad: Boolean = false) = repository.getSeriesData(href, hrefTitle, forceLoad)

	fun updateSeriesFavorite(seriesData: SeriesWithInfo) = viewModelScope.launch(Dispatchers.IO) {
		repository.updateSeriesFavorite(seriesData.series)
	}

	fun getStream(list: List<HosterData>) = m3ORepository.getAnyStream(list)
}