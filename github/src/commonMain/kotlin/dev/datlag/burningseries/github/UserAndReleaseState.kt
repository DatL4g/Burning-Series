package dev.datlag.burningseries.github

import dev.datlag.burningseries.github.model.RESTRelease
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.tooling.getDigitsOrNull

sealed interface UserAndReleaseState {

    val user: UserAndRelease.User?
        get() = null

    fun release(currentVersion: String?): UserAndRelease.Release? {
        return null
    }

    data object None : UserAndReleaseState
    data class Success(val data: UserAndRelease) : UserAndReleaseState {

        override val user: UserAndRelease.User?
            get() = data.user

        constructor(release: RESTRelease) : this(UserAndRelease(release))

        override fun release(currentVersion: String?): UserAndRelease.Release? {
            if (currentVersion.isNullOrBlank()) {
                return null
            }
            val releaseNumber = data.release?.tagNumber ?: return null
            val currentNumber = currentVersion.getDigitsOrNull()?.toIntOrNull() ?: return null

            if (releaseNumber > currentNumber) {
                return data.release
            }
            return null
        }
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