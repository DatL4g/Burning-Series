package dev.datlag.burningseries.github

import com.apollographql.apollo3.ApolloClient
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.tooling.async.suspendCatching
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class UserAndReleaseRepository(
    private val client: ApolloClient,
    private val github: GitHub,
    private val saveUser: (UserAndRelease.User) -> Unit
) {

    private val query = Query()
    val userAndRelease = client.query(query.toGraphQL()).toFlow().mapNotNull {
        val data = it.data
        if (data == null) {
            if (it.hasErrors()) {
                UserAndReleaseState.fromGraphQL(data, it.exception)
            } else {
                UserAndReleaseState.Error(it.exception)
            }
        } else {
            UserAndReleaseState.fromGraphQL(data, it.exception)
        }
    }.map {
        it.user?.let(saveUser)

        when (it) {
            is UserAndReleaseState.Error -> {
                suspendCatching {
                    github.getLatestRelease(query.owner, query.repo)
                }.getOrNull()?.let { release ->
                    UserAndReleaseState.Success(release)
                } ?: it
            }
            else -> it
        }
    }

    private data class Query(
        val owner: String = Constants.OWNER,
        val repo: String = Constants.REPO
    ) {
        fun toGraphQL() = UserAndReleaseQuery(
            owner = owner,
            repo = repo
        )
    }
}