package de.datlag.network.github

import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.model.github.Release
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@Obfuscate
class GitHubRepository @Inject constructor(
    private val service: GitHub
) {

    fun getReleases(): Flow<List<Release>> = flow<List<Release>> {
        networkResource(
            fetchFromRemote = {
                service.getReleases()
            }
        ).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> emit((it.data ?: listOf()).toMutableList().filterNot { release -> release.isDraft })
                else -> emit(emptyList())
            }
        }
    }.flowOn(Dispatchers.IO)
}