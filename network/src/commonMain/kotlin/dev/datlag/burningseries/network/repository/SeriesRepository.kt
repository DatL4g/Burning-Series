package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.ApiSuccessResponse
import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.dbBoundResource
import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.model.LoggingMode
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.common.trimHref
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.network.bs.BsScraper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import dev.datlag.burningseries.network.common.Dispatchers
import io.ktor.client.*

class SeriesRepository(
    private val api: BurningSeries,
    override val logger: ActionLogger,
    private val client: HttpClient
) : LogRepository {

    override val mode: Int = LoggingMode.SERIES

    val seriesState: MutableStateFlow<Series?> = MutableStateFlow(null)

    private val seriesHref: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _series: Flow<Resource<Series?>> = seriesHref.transformLatest {
        if (it != null) {
            return@transformLatest emitAll(dbBoundResource(
                makeNetworkRequest = {
                    info("Loading series data")
                    BsScraper.client(client).logger(logger).getSeries(it)?.let { series ->
                        ApiSuccessResponse(series, emptySet())
                    } ?: run {
                        warning("Could not scrape series on-device")
                        api.series(it)
                    }
                },
                fetchFromLocal = {
                    seriesState
                },
                shouldMakeNetworkRequest = { series ->
                    series == null || !series.href.trimHref().equals(it.trimHref(), true)
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
                error("Could not load series: (${it.statusCode}) ${it.message}")
                it.data
            }
            is Resource.Status.EmptySuccess -> {
                warning("Got empty response when loading series")
                null
            }
            is Resource.Status.Success -> {
                it.data
            }
        })
    }.flowOn(Dispatchers.IO).stateIn(GlobalScope, SharingStarted.WhileSubscribed(), null)

    suspend fun loadFromHref(href: String) {
        info("Load series by href: $href")
        seriesHref.emit(href)
    }


}