package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.model.Release
import dev.datlag.burningseries.model.common.getDigitsOrNull
import dev.datlag.burningseries.network.GitHub
import dev.datlag.burningseries.network.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class GitHubRepository(
    private val api: GitHub
) {

    private val ownerAndRepo: MutableStateFlow<Pair<String, String>?> = MutableStateFlow(null)
    private val installedVersion: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _releases: Flow<Resource<List<Release>>> = ownerAndRepo.transformLatest {
        if (it != null) {
            return@transformLatest emitAll(networkResource(
                makeNetworkRequest = {
                    api.getReleases(it.first, it.second)
                }
            ).distinctUntilChanged())
        }
    }.flowOn(Dispatchers.IO)

    private val _status = _releases.transformLatest {
        return@transformLatest emit(it.status)
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    val status = _status.transformLatest {
        return@transformLatest emit(Status.create(it, true))
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    val releases = _status.transformLatest {
        return@transformLatest emit(when (it) {
            is Resource.Status.Loading -> it.data ?: emptyList()
            is Resource.Status.Error -> {
                println(ownerAndRepo.value?.first.toString() + ", " + ownerAndRepo.value?.second.toString())
                println(it.message)
                it.data ?: emptyList()
            }
            is Resource.Status.Success -> it.data
            is Resource.Status.EmptySuccess -> emptyList()
        })
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    val newRelease = releases.transformLatest { releases ->
        val currentVersion = installedVersion.value

        return@transformLatest emit(if (currentVersion != null) {
            releases.filter {
                (it.tagAsNumberString()?.toIntOrNull() ?: 0) > (currentVersion.toIntOrNull() ?: 0)
            }.maxByOrNull { it.publishedAtSeconds }
        } else {
            releases.maxByOrNull { it.publishedAtSeconds } ?: releases.firstOrNull()
        })
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    suspend fun loadReleases(
        installedVersion: String?,
        owner: String,
        repo: String
    ) {
        this.installedVersion.emit(installedVersion?.getDigitsOrNull() ?: installedVersion)
        this.ownerAndRepo.emit(owner to repo)
    }
}