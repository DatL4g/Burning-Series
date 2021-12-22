@file:Obfuscate

package de.datlag.burningseries.common

import io.michaelrocks.paranoid.Obfuscate

fun Collection<*>.isLargerThan(size: Int) = this.size > size