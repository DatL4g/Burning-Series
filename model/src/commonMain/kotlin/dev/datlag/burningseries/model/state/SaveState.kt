package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.HosterScraping

sealed interface SaveState {
    data object Waiting : SaveState
    data class Saving(val data: HosterScraping) : SaveState
}

sealed interface SaveAction {
    data class Save(val data: HosterScraping) : SaveAction
}