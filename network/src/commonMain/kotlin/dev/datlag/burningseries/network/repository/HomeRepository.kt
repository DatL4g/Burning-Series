package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.ApiSuccessResponse
import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.dbBoundResource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.network.bs.BsScraper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import dev.datlag.burningseries.network.common.Dispatchers
import io.ktor.client.*

class HomeRepository(
    private val api: BurningSeries,
    override val logger: ActionLogger,
    private val client: HttpClient
) : LogRepository {

    override val mode: Int = 1

    val homeState: MutableStateFlow<Home> = MutableStateFlow(Home())

    private val home: Flow<Resource<Home>> = dbBoundResource(
        makeNetworkRequest = {
            info("Loading home data")
            BsScraper.client(client).logger(logger).getHome(String())?.let { home ->
                ApiSuccessResponse(home, emptySet())
            } ?: run {
                warning("Could not scrape home data on-device")
                api.home()
            }
        },
        fetchFromLocal = {
            homeState
        },
        shouldMakeNetworkRequest = { it == null || (it.episodes.isEmpty() && it.series.isEmpty()) },
        saveResponseData = {
            homeState.emit(it)
        },
    ).flowOn(Dispatchers.IO).stateIn(GlobalScope, SharingStarted.Lazily, Resource.loading())

    private val _status = home.transform {
        return@transform emit(it.status)
    }.flowOn(Dispatchers.IO)

    val status = _status.transform {
        return@transform emit(Status.create(it))
    }

    val episodes = _status.transform {
        return@transform emit(when (it) {
            is Resource.Status.Loading -> {
                it.data?.episodes ?: emptyList()
            }
            is Resource.Status.EmptySuccess -> {
                warning("Got empty response when loading home episode data")
                emptyList()
            }
            is Resource.Status.Success -> {
                it.data.episodes
            }
            is Resource.Status.Error -> {
                error("Could not load home episode data: (${it.statusCode}) ${it.message}")
                it.data?.episodes ?: emptyList()
            }
        })
    }.flowOn(Dispatchers.IO)

    val series = _status.transform {
        return@transform emit(when (it) {
            is Resource.Status.Loading -> {
                it.data?.series ?: emptyList()
            }
            is Resource.Status.EmptySuccess -> {
                warning("Got empty response when loading home series data")
                emptyList()
            }
            is Resource.Status.Success -> {
                it.data.series
            }
            is Resource.Status.Error -> {
                error("Could not load home series data: (${it.statusCode}) ${it.message}")
                it.data?.series ?: emptyList()
            }
        })
    }.flowOn(Dispatchers.IO)
}