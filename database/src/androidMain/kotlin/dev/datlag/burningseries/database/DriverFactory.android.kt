package dev.datlag.burningseries.database

import android.content.Context
import android.content.ContextWrapper
import androidx.sqlite.db.SupportSQLiteOpenHelper
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.datlag.tooling.existsSafely
import dev.datlag.tooling.isDirectorySafely
import dev.datlag.tooling.mkdirsSafely
import dev.datlag.tooling.parentSafely
import java.io.File

actual class DriverFactory(
    context: Context
) {

    private val databaseContext = object : ContextWrapper(context) {
        override fun getDatabasePath(name: String?): File {
            val superFile = super.getDatabasePath(name)
            val (defaultFolder, defaultName) = if (superFile.isDirectorySafely()) {
                (superFile ?: context.filesDir) to (name ?: "database.db")
            } else {
                (superFile?.parentSafely() ?: context.filesDir) to (name?.ifBlank { null } ?: superFile.name)
            }

            val versionedFolder = File(defaultFolder, "v6")
            if (!versionedFolder.existsSafely()) {
                versionedFolder.mkdirsSafely()
            }
            return File(versionedFolder, defaultName)
        }
    }

    actual fun createBurningSeriesDriver(): SqlDriver {
        val driver = AndroidSqliteDriver(BurningSeries.Schema, databaseContext, "bs.db")

        return driver
    }
}