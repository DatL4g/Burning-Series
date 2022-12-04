package dev.datlag.burningseries.other

sealed interface Orientation {
    object PORTRAIT : Orientation
    object LANDSCAPE : Orientation
}