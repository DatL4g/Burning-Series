package dev.datlag.burningseries.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(
    private val context: Context
) {
    actual fun createBurningSeriesDriver(): SqlDriver {
        return AndroidSqliteDriver(BurningSeries.Schema, context, "bs.db")
    }

    fun createBurningSeriesNativeDriver(): SupportSQLiteOpenHelper {
        val factory = FrameworkSQLiteOpenHelperFactory()
        val callback = AndroidSqliteDriver.Callback(BurningSeries.Schema)

        return factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .callback(callback)
                .name("bs.db")
                .noBackupDirectory(false)
                .build()
        )
    }

    fun createSeriesQuery(
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out Any>?,
        sortOrder: String?
    ): SupportSQLiteQuery {
        return SupportSQLiteQueryBuilder.builder("Series")
            .columns(projection)
            .selection(selection, selectionArgs)
            .orderBy(sortOrder)
            .create()
    }

    fun createEpisodeQuery(
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out Any>?,
        sortOrder: String?
    ): SupportSQLiteQuery {
        return SupportSQLiteQueryBuilder.builder("Episode")
            .columns(projection)
            .selection(selection, selectionArgs)
            .orderBy(sortOrder)
            .create()
    }
}