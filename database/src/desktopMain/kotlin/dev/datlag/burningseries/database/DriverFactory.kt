package dev.datlag.burningseries.database

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        // ToDo("not in-memory")
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BurningSeriesDB.Schema.create(driver)
        return driver
    }
}