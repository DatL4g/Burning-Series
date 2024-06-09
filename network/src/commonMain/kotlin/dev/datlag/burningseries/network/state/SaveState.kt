package dev.datlag.burningseries.network.state

import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.skeo.Stream

sealed interface SaveState {
    data object None : SaveState
    data class Saving(val data: HosterScraping) : SaveState
    data class Success(val stream: Stream?) : SaveState
    data class Error(val stream: Stream?) : SaveState
}

sealed interface SaveAction {
    data object Clear : SaveAction
    data class Save(val data: HosterScraping) : SaveAction
}