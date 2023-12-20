package dev.datlag.burningseries.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface Shortcut {

    @Serializable
    sealed interface Intent : Shortcut {

        @Serializable
        data object SEARCH : Intent

        @Serializable
        data class Series(val href: String) : Intent

        @Serializable
        data object NONE : Intent
    }
}