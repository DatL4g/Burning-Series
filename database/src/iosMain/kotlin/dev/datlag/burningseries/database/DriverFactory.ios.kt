package dev.datlag.burningseries.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createBurningSeriesDriver(): SqlDriver {
        return NativeSqliteDriver(BurningSeries.Schema, "bs.db")
    }
}