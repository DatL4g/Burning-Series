@file:Obfuscate

package de.datlag.network.common

import io.michaelrocks.paranoid.Obfuscate

fun <T> MutableList<T>.mapInPlace(mutator: (T) -> T) {
    val iterate = this.listIterator()
    while (iterate.hasNext()) {
        val oldValue = iterate.next()
        val newValue = mutator(oldValue)
        if (newValue != oldValue) {
            iterate.set(newValue)
        }
    }
}