package dev.datlag.mimasu.extension.provider.streamkiste

sealed interface Type {

    val name: String

    data object Movie : Type {
        override val name: String = "movies"
    }

    data object TV : Type {
        override val name: String = "tvseries"
    }
}