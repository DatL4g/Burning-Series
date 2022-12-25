package dev.datlag.burningseries.database

import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
     fun createDriver(): SqlDriver
}