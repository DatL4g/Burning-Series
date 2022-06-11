package de.datlag.network.github

import com.apollographql.apollo3.ApolloClient
import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.model.Constants
import de.datlag.model.github.Release
import de.datlag.model.github.User
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Named

@Obfuscate
class GitHubRepository @Inject constructor(
    private val service: GitHub,
    @Named("githubApollo") private val apolloClient: ApolloClient
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

    fun getUser(token: String): Flow<User?> = flow {
        networkResource(
            fetchFromRemote = {
                service.getUser("token $token")
            }
        ).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> emit(it.data)
                else -> emit(null)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun isSponsoring(login: String, token: String) : Flow<Boolean> = flow<Boolean> {
        val apolloClientWithToken = apolloClient.newBuilder().addHttpHeader("Authorization", "Bearer $token").build()
        val response = apolloClientWithToken.query(SponsoringQuery(login)).execute()
        val data = response.data?.user?.sponsoring?.nodes?.mapNotNull { it?.onUser?.login } ?: listOf()
        emit(data.any { it.equals(Constants.GITHUB_OWNER, true) })
    }.flowOn(Dispatchers.IO)
}