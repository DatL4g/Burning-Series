@file:Obfuscate

package de.datlag.network.common

import io.michaelrocks.paranoid.Obfuscate

fun Boolean.toInt() = if (this) 1 else 0