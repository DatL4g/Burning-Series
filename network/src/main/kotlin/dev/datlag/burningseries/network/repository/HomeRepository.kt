package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.network.BurningSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*

class HomeRepository(
    private val api: BurningSeries
) {
    private val home: Flow<Resource<Home>> = networkResource(
        makeNetworkRequest = { api.home() }
    ).flowOn(Dispatchers.IO).stateIn(GlobalScope, SharingStarted.Lazily, Resource.loading())

    private val _status = home.transform {
        return@transform emit(it.status)
    }.flowOn(Dispatchers.IO)

    val status = _status.transform {
        return@transform emit(when (it) {
            is Resource.Status.Loading -> Status.LOADING
            is Resource.Status.Error -> Status.ERROR
            is Resource.Status.EmptySuccess -> Status.SUCCESS
            is Resource.Status.Success -> Status.SUCCESS
        })
    }

    val episodes = _status.transform {
        return@transform emit(when (it) {
            is Resource.Status.Loading -> {
                it.data?.episodes ?: emptyList()
            }
            is Resource.Status.EmptySuccess -> {
                emptyList()
            }
            is Resource.Status.Success -> {
                it.data.episodes
            }
            is Resource.Status.Error -> {
                it.data?.episodes ?: emptyList()
            }
        })
    }.flowOn(Dispatchers.IO)

    val series = _status.transform {
        return@transform emit(when (it) {
            is Resource.Status.Loading -> {
                it.data?.series ?: emptyList()
            }
            is Resource.Status.EmptySuccess -> emptyList()
            is Resource.Status.Success -> {
                it.data.series
            }
            is Resource.Status.Error -> {
                it.data?.series ?: emptyList()
            }
        })
    }.flowOn(Dispatchers.IO)

    sealed class Status {
        object LOADING : Status()
        object ERROR : Status()
        object SUCCESS : Status()
    }
}