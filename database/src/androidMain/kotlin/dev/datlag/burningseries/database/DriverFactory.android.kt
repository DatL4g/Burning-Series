package dev.datlag.burningseries.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(
    private val context: Context
) {
    actual fun createBurningSeriesDriver(): SqlDriver {
        val driver = AndroidSqliteDriver(BurningSeries.Schema, context, "v6/bs.db")

        return driver
    }
}