@file:Obfuscate

package de.datlag.model.common

import io.michaelrocks.paranoid.Obfuscate
import kotlinx.datetime.LocalDate

fun LocalDate.asIsoString() = "%d-%02d-%02d".format(this.year, this.monthNumber, this.dayOfMonth)