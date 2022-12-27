package dev.datlag.burningseries.model.common

import kotlinx.datetime.LocalDate

fun LocalDate.asIsoString() = "%d-%02d-%02d".format(this.year, this.monthNumber, this.dayOfMonth)