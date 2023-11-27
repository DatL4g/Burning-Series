package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.burningseries.model.Stream

sealed interface SaveState {
    data object Waiting : SaveState
    data class Saving(val data: HosterScraping, val loadStream: Boolean) : SaveState
    data class Success(val stream: Stream?) : SaveState
    data object Error : SaveState
}

sealed interface SaveAction {
    data class Save(val data: HosterScraping, val loadStream: Boolean = true) : SaveAction
}