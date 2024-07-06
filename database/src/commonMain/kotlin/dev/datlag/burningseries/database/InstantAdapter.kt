package dev.datlag.burningseries.database

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object InstantAdapter : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant {
        return if (databaseValue <= 0L) {
            Clock.System.now()
        } else {
            Instant.fromEpochSeconds(databaseValue)
        }
    }

    override fun encode(value: Instant): Long {
        return value.epochSeconds
    }
}