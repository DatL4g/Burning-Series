package dev.datlag.burningseries.model.common

import kotlinx.datetime.LocalDate

fun LocalDate.asIsoString(): String {
    fun doubleDigit(value: Int): String {
        return if (value <= 9) {
            "0$value"
        } else {
            "$value"
        }
    }

    return "${this.year}-${doubleDigit(this.monthNumber)}-${doubleDigit(this.dayOfMonth)}"
}