package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.BurningSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.JsonElement
import java.net.URLEncoder

class SeriesRepository(
    private val api: BurningSeries
) {
    private val seriesHref: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _series: Flow<Resource<Series>> = seriesHref.debounce(500).transformLatest {
        if (it != null) {
            return@transformLatest emitAll(networkResource(
                makeNetworkRequest = {
                    api.series(it)
                }
            ))
        }
    }.flowOn(Dispatchers.IO)

    private val _status = _series.transform {
        return@transform emit(it.status)
    }.flowOn(Dispatchers.IO)

    val status = _status.transform {
        return@transform emit(when (it) {
            is Resource.Status.Loading -> Status.LOADING
            is Resource.Status.Error -> Status.ERROR
            is Resource.Status.EmptySuccess -> Status.SUCCESS
            is Resource.Status.Success -> Status.SUCCESS
        })
    }.flowOn(Dispatchers.IO)

    val series = _status.transform {
        return@transform emit(when (it) {
            is Resource.Status.Loading -> {
                it.data
            }
            is Resource.Status.Error -> {
                it.data
            }
            is Resource.Status.EmptySuccess -> {
                null
            }
            is Resource.Status.Success -> {
                it.data
            }
        })
    }.flowOn(Dispatchers.IO).stateIn(GlobalScope, SharingStarted.WhileSubscribed(), null)

    suspend fun loadFromHref(href: String) {
        seriesHref.emit(href)
    }

    sealed class Status {
        object LOADING : Status()
        object ERROR : Status()
        object SUCCESS : Status()
    }
}