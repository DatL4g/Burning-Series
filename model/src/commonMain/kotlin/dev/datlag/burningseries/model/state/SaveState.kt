package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.skeo.Stream

sealed interface SaveState {
    data object Waiting : SaveState
    data class Saving(val data: HosterScraping, val loadStream: Boolean) : SaveState
    data class Success(val stream: Stream?) : SaveState
    data class Error(val stream: Stream?) : SaveState
}

sealed interface SaveAction {
    data class Save(val data: HosterScraping, val loadStream: Boolean = true) : SaveAction
}