package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.Series

sealed interface SeriesState {
    data class Loading(val href: String) : SeriesState
    data class Success(val series: Series) : SeriesState
    data class Error(val msg: String) : SeriesState
}

sealed interface SeriesAction {
    data object Retry : SeriesAction
}