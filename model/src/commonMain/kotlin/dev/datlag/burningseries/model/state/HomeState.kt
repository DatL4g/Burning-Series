package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.Home

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val home: Home, val onDeviceReachable: Boolean) : HomeState
    data object Error : HomeState
}

sealed interface HomeAction {
    data object Retry : HomeAction
}