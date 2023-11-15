package dev.datlag.burningseries.database

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createBurningSeriesDriver(): SqlDriver
}