package dev.datlag.burningseries.network.state

import dev.datlag.burningseries.model.Home

sealed interface HomeState {

    val isLoading: Boolean
        get() = this !is PostLoading

    data object Loading : HomeState

    sealed interface PostLoading : HomeState

    data class Success(val home: Home) : PostLoading

    data class Failure(
        internal val throwable: Throwable?
    ) : PostLoading

    companion object {
        fun fromResult(result: Result<Home?>): HomeState {
            return result.getOrNull()?.let(::Success) ?: Failure(result.exceptionOrNull())
        }
    }
}

sealed interface HomeAction {
    data object Retry : HomeAction
}