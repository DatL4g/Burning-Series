package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.Genre

sealed interface SearchState {
    data object Loading : SearchState
    data class Success(val genres: List<Genre>) : SearchState
    data class Error(val msg: String) : SearchState
}

sealed interface SearchAction {
    data object Retry : SearchAction
}