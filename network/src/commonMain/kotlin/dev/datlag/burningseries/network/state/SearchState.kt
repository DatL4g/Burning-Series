package dev.datlag.burningseries.network.state

import dev.datlag.burningseries.model.SearchItem
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet

sealed interface SearchState {

    val isLoading: Boolean
        get() = this !is PostLoading

    val isError: Boolean
        get() = this is Failure

    val hasQueryItems: Boolean
        get() = this is Success && this.queriedItems.isNotEmpty()

    data object Loading : SearchState

    sealed interface PostLoading : SearchState

    data class Success(
        val allItems: ImmutableSet<SearchItem>,
        val queriedItems: ImmutableSet<SearchItem> = persistentSetOf(),
    ) : PostLoading

    data class Failure(
        internal val throwable: Throwable?
    ) : PostLoading

    companion object {
        fun fromResult(result: Result<ImmutableCollection<SearchItem>?>): SearchState {
            return result.getOrNull()?.let { col ->
                if (col.isEmpty()) {
                    Failure(result.exceptionOrNull())
                } else {
                    Success(col.toImmutableSet())
                }
            } ?: Failure(
                result.exceptionOrNull()
            )
        }
    }
}

sealed interface SearchAction {
    data object Retry : SearchAction

    data class Query(val query: String?) : SearchAction
}