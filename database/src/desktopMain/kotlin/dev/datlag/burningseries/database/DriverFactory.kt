package dev.datlag.burningseries.database

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.io.File

actual class DriverFactory(private val file: File) {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}")
        BurningSeriesDB.Schema.create(driver)
        return driver
    }
}