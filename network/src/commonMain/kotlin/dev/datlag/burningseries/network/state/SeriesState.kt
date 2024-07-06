package dev.datlag.burningseries.network.state

import dev.datlag.burningseries.model.Series

sealed interface SeriesState {

    val isLoading: Boolean
        get() = this !is PostLoading

    data object Loading : SeriesState

    sealed interface PostLoading : SeriesState

    data class Success(val series: Series) : PostLoading

    data class Failure(
        internal val throwable: Throwable?
    ) : PostLoading

    companion object {
        fun fromResult(result: Result<Series?>): SeriesState {
            return result.getOrNull()?.let(::Success) ?: Failure(result.exceptionOrNull())
        }
    }
}

sealed interface SeriesAction {
    data object Retry : SeriesAction
}