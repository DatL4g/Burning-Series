package de.datlag.burningseries.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.burningseries.common.indexOfLastWithItem
import de.datlag.burningseries.common.toMutableSharedFlow
import de.datlag.model.burningseries.Cover
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.allseries.relation.GenreWithItems
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.series.*
import de.datlag.model.burningseries.series.relation.EpisodeWithHoster
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.network.burningseries.BurningSeriesRepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class BurningSeriesViewModel @Inject constructor(
	val repository: BurningSeriesRepository
): ViewModel() {

	var showedHelpImprove: Boolean = false

	val homeData = repository.getHomeData()
	private val _favorites: MutableSharedFlow<List<SeriesWithInfo>> = repository.getSeriesFavorites().toMutableSharedFlow(viewModelScope)
	val favorites = _favorites.asSharedFlow()

	val allSeriesCount: StateFlow<Long> = repository.getAllSeriesCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0L)
	private val _allSeriesPagination: MutableStateFlow<Long> = MutableStateFlow(0)
	private val _allSeriesPaginated: MutableSharedFlow<Resource<List<GenreWithItems>>> = MutableSharedFlow()
	private val _allSeriesPaginatedFlat: MutableSharedFlow<Pair<Boolean, List<GenreModel>>> = _allSeriesPaginated.mapNotNull { it.data?.flatMap { item -> item.toGenreModel() }?.let { list -> true to list } }.toMutableSharedFlow(viewModelScope)
	val allSeriesPagination = _allSeriesPagination.asSharedFlow()
	val allSeriesPaginatedFlat = _allSeriesPaginatedFlat.asSharedFlow()

	private val _seriesStatus: MutableStateFlow<Resource.Status> = MutableStateFlow(Resource.Status.LOADING)
	private val _seriesData: MutableStateFlow<SeriesWithInfo?> = MutableStateFlow(null)

	val seriesStatus: SharedFlow<Resource.Status> = _seriesStatus.asSharedFlow()
	val seriesData: SharedFlow<SeriesWithInfo?> = _seriesData.asSharedFlow()

	val currentSeriesData: SeriesWithInfo?
		get() = _seriesData.value
	val currentSeriesLanguages: List<LanguageData>
		get() {
			val series = currentSeriesData
			return if (series == null) {
				emptyList()
			} else if (!series.languages.isNullOrEmpty()) {
				series.languages
			} else {
				series.series.languages
			}
		}
	val currentSeriesSeasons: List<SeasonData>
		get() {
			val series = currentSeriesData
			return if (series == null) {
				emptyList()
			} else if (!series.seasons.isNullOrEmpty()) {
				series.seasons
			} else {
				series.series.seasons
			}
		}
	val currentSeriesEpisodes: List<EpisodeWithHoster>
		get() {
			val series = currentSeriesData
			return if (series == null) {
				emptyList()
			} else if (!series.episodes.isNullOrEmpty()) {
				series.episodes
			} else {
				series.series.episodes.map { EpisodeWithHoster(it, it.hoster) }
			}
		}
	val seriesBSImage: Flow<Cover> = seriesData.mapNotNull { it?.cover ?: it?.series?.cover }.distinctUntilChanged()
	val seriesTitle: Flow<String> = seriesData.map { it?.series?.title ?: String() }.distinctUntilChanged()
	val seriesFavorite: StateFlow<Boolean> = seriesData.map { (it?.series?.favoriteSince ?: 0) > 0 }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
	val seriesLanguages: Flow<List<LanguageData>> = seriesData.map {
		it?.let { seriesWithInfo ->
			return@let if (!seriesWithInfo.languages.isNullOrEmpty()) {
				seriesWithInfo.languages
			} else {
				seriesWithInfo.series.languages
			}
		} ?: emptyList()
	}.distinctUntilChanged()
	val seriesSelectedLanguage: Flow<LanguageData?> = seriesData.map {
		it?.languages?.firstOrNull { lang -> lang.value == it.series.selectedLanguage } ?: it?.languages?.getOrNull(0)
	}.distinctUntilChanged()
	val seriesSeasons: Flow<List<SeasonData>> = seriesData.map {
		it?.let { seriesWithInfo ->
			return@let if (!seriesWithInfo.seasons.isNullOrEmpty()) {
				seriesWithInfo.seasons
			} else {
				seriesWithInfo.series.seasons
			}
		} ?: emptyList()
	}.distinctUntilChanged()
	val seriesSelectedSeason: Flow<SeasonData?> = seriesData.map {
		it?.series?.currentSeason(currentSeriesSeasons)
	}.distinctUntilChanged()
	val seriesDescription: Flow<String> = seriesData.map { it?.series?.description ?: String() }.distinctUntilChanged()
	val seriesEpisodes: Flow<List<EpisodeWithHoster>> = seriesData.map {
		it?.let { seriesWithInfo ->
			return@let if (!seriesWithInfo.episodes.isNullOrEmpty()) {
				seriesWithInfo.episodes
			} else {
				seriesWithInfo.series.episodes.map { episodeInfo ->
					EpisodeWithHoster(episodeInfo, episodeInfo.hoster)
				}
			}
		} ?: emptyList()
	}.distinctUntilChanged()
	val seriesInfo: Flow<List<InfoData>> = seriesData.map {
		it?.let { seriesWithInfo ->
			return@let if (!seriesWithInfo.infos.isNullOrEmpty()) {
				seriesWithInfo.infos
			} else {
				seriesWithInfo.series.infos
			}
		} ?: emptyList()
	}.distinctUntilChanged()
	val continueSeriesEpisode: EpisodeWithHoster?
		get() {
			val episodeList = currentSeriesEpisodes.sortedWith(compareBy<EpisodeWithHoster> { it.episode.number.toIntOrNull() }.thenBy { it.episode.number })
			val lastWatched = episodeList.indexOfLastWithItem { it.episode.watchedPercentage() > 0 }
			return when {
				lastWatched.first == -1 -> episodeList.firstOrNull()
				lastWatched.second?.episode?.finishedWatching == true -> {
					episodeList.getOrNull(lastWatched.first + 1) ?: lastWatched.second
				}
				else -> lastWatched.second
			} ?: lastWatched.second ?: episodeList.firstOrNull()
		}

	private val _genres: MutableStateFlow<List<GenreModel.GenreData>> = MutableStateFlow(listOf())
	val genres: List<GenreModel.GenreData>
		get() = _genres.value

	private var fetchSeriesJob: Job? = null

	fun getAllGenres() = viewModelScope.launch(Dispatchers.IO) {
		_genres.emitAll(repository.getAllGenres())
	}

	fun setSeriesData(seriesWithInfo: SeriesWithInfo?) {
		val success = _seriesData.tryEmit(seriesWithInfo)
		if (!success) {
			viewModelScope.launch(Dispatchers.IO) {
				_seriesData.emit(seriesWithInfo)
			}
		}
	}

	fun setAllSeriesPage(index: Int) = viewModelScope.launch(Dispatchers.IO) {
		_allSeriesPagination.emit(index.toLong())
	}

	fun getAllSeriesNext() = viewModelScope.launch(Dispatchers.IO) {
		val maxValue = repository.getAllSeriesCount().first()
		if (_allSeriesPagination.value + 1 < maxValue) {
			_allSeriesPagination.emit(_allSeriesPagination.value + 1)
		} else {
			_allSeriesPagination.emit(0)
		}
	}

	fun getAllSeriesPrevious() = viewModelScope.launch(Dispatchers.IO) {
		val maxValue = repository.getAllSeriesCount().first()
		if (_allSeriesPagination.value - 1 < 0) {
			_allSeriesPagination.emit(maxValue - 1)
		} else {
			_allSeriesPagination.emit(_allSeriesPagination.value -1)
		}
	}

	fun getNewPaginationData() = viewModelScope.launch(Dispatchers.IO) {
		_allSeriesPaginated.emitAll(repository.getAllSeries(_allSeriesPagination.value))
	}

	fun searchAllSeries(title: String) = viewModelScope.launch(Dispatchers.IO) {
		repository.searchAllSeries(title).collect {
			_allSeriesPaginatedFlat.emit(false to it)
		}
	}

	fun getAllFavorites() = viewModelScope.launch(Dispatchers.IO) {
		_favorites.emitAll(repository.getSeriesFavorites())
	}

	fun searchFavorites(title: String) = viewModelScope.launch(Dispatchers.IO) {
		_favorites.emitAll(repository.searchSeriesFavorites(title))
	}

	fun getSeriesData(latestSeries: LatestSeries) {
		fetchSeriesJob?.cancel()
		fetchSeriesJob = viewModelScope.launch(Dispatchers.IO) {
			repository.getSeriesData(latestSeries).collect {
				val safe = it.data
				if (safe != null) {
					setSeriesData(safe)
				} else {
					_seriesData.emit(it.data)
				}
				_seriesStatus.emit(it.status)
			}
		}
	}

	fun getSeriesData(latestEpisode: LatestEpisode) {
		fetchSeriesJob?.cancel()
		fetchSeriesJob = viewModelScope.launch(Dispatchers.IO) {
			repository.getSeriesData(latestEpisode).collect {
				val safe = it.data
				if (safe != null) {
					setSeriesData(safe)
				} else {
					_seriesData.emit(it.data)
				}
				_seriesStatus.emit(it.status)
			}
		}
	}

	fun getSeriesData(genreItem: GenreModel.GenreItem) {
		fetchSeriesJob?.cancel()
		fetchSeriesJob = viewModelScope.launch(Dispatchers.IO) {
			repository.getSeriesData(genreItem).collect {
				val safe = it.data
				if (safe != null) {
					setSeriesData(safe)
				} else {
					_seriesData.emit(it.data)
				}
				_seriesStatus.emit(it.status)
			}
		}
	}

	fun getSeriesData(href: String, hrefTitle: String, forceLoad: Boolean = false) {
		fetchSeriesJob?.cancel()
		fetchSeriesJob = viewModelScope.launch(Dispatchers.IO) {
			repository.getSeriesData(href, hrefTitle, forceLoad).collect {
				val safe = it.data
				if (safe != null) {
					setSeriesData(safe)
				} else {
					_seriesData.emit(it.data)
				}
				_seriesStatus.emit(it.status)
			}
		}
	}

	fun cancelFetchSeries() = fetchSeriesJob?.cancel()

	fun updateSeriesFavorite(seriesData: SeriesWithInfo) = viewModelScope.launch(Dispatchers.IO) {
		repository.updateSeriesFavorite(seriesData.series)
	}

	fun updateEpisodeInfo(episodeInfo: EpisodeInfo) = viewModelScope.launch(Dispatchers.IO) {
		repository.updateEpisodeInfo(episodeInfo)
	}

	fun getStream(list: List<HosterData>) = repository.getStreams(list.map { it.href })

	fun getAllSeriesCountJoined() = repository.getAllSeriesCountJoined()

	fun getSeriesCount() = repository.getSeriesCount()
}