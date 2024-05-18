package dev.datlag.burningseries.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.datlag.burningseries.model.common.scopeCatching
import java.io.File

actual class DriverFactory(private val file: File) {
    actual fun createBurningSeriesDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${file.canonicalPath}")
        BurningSeries.Schema.create(driver)

        scopeCatching {
            BurningSeries.Schema.migrate(
                driver = driver,
                oldVersion = 0,
                newVersion = BurningSeries.Schema.version
            )
        }
        return driver
    }
}