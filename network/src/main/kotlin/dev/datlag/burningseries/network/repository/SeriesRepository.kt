package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.dbBoundResource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.JsonElement
import java.net.URLEncoder

class SeriesRepository(
    private val api: BurningSeries
) {
    val seriesState: MutableStateFlow<Series?> = MutableStateFlow(null)

    private val seriesHref: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _series: Flow<Resource<Series?>> = seriesHref.transformLatest {
        if (it != null) {
            return@transformLatest emitAll(dbBoundResource(
                makeNetworkRequest = {
                    api.series(it)
                },
                fetchFromLocal = {
                    seriesState
                },
                shouldMakeNetworkRequest = { series ->
                    series == null
                },
                saveResponseData = { series ->
                    seriesState.emit(series)
                }
            ))
        }
    }.flowOn(Dispatchers.IO)

    private val _status = _series.transform {
        return@transform emit(it.status)
    }.flowOn(Dispatchers.IO)

    val status = _status.transform {
        return@transform emit(Status.create(it))
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


}