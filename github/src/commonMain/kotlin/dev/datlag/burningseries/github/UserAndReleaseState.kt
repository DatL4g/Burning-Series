package dev.datlag.burningseries.github

import dev.datlag.burningseries.github.model.RESTRelease
import dev.datlag.burningseries.github.model.UserAndRelease

sealed interface UserAndReleaseState {
    data object None : UserAndReleaseState
    data class Success(val data: UserAndRelease) : UserAndReleaseState {
        constructor(release: RESTRelease) : this(UserAndRelease(release))
    }
    data class Error(val throwable: Throwable?) : UserAndReleaseState

    companion object {
        fun fromGraphQL(
            query: UserAndReleaseQuery.Data?,
            throwable: Throwable?
        ): UserAndReleaseState {
            val data = query?.let(::UserAndRelease) ?: return Error(throwable)

            return Success(data)
        }
    }
}