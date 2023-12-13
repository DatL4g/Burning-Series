package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.Series

sealed interface SeriesState {

    val href: String

    data class Loading(override val href: String) : SeriesState
    data class Success(val series: Series, val onDeviceReachable: Boolean) : SeriesState {
        override val href: String = series.href
    }
    data class Error(override val href: String) : SeriesState
}

sealed interface SeriesAction {
    data object Retry : SeriesAction
    data class Load(val href: String) : SeriesAction
}