package dev.datlag.burningseries.common

import dev.datlag.burningseries.database.DBSeries

fun DBSeries.coverFileName(): String {
    return this.hrefTitle.split(
        '/',
        limit = 2
    ).joinToString(
        "/",
        limit = 2,
        truncated = String()
    ).fileName() + ".bs"
}