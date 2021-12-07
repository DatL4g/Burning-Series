package de.datlag.executor

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
sealed class Schema {
    object Conflated : Schema()
    object Queue : Schema()
}
