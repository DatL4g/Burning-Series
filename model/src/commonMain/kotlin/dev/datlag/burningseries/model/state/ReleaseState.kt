package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.Release

sealed interface ReleaseState {
    data object Loading : ReleaseState
    data class Success(val releases: List<Release>) : ReleaseState
    data object Error : ReleaseState
}