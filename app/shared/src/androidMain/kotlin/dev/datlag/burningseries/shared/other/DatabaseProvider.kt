package dev.datlag.burningseries.shared.other

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import dev.datlag.burningseries.database.DriverFactory

class DatabaseProvider : ContentProvider() {

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private lateinit var db: DriverFactory
    private val database by lazy {
        db.createBurningSeriesNativeDriver().readableDatabase
    }

    init {
        uriMatcher.addURI(
            PROVIDER_NAME,
            "series",
            SERIES_URI_CODE
        )
        uriMatcher.addURI(
            PROVIDER_NAME,
            "series/*",
            SERIES_URI_CODE
        )

        uriMatcher.addURI(
            PROVIDER_NAME,
            "episodes",
            EPISODES_URI_CODE
        )
        uriMatcher.addURI(
            PROVIDER_NAME,
            "episodes/*",
            EPISODES_URI_CODE
        )
    }

    override fun onCreate(): Boolean {
        context?.let {
            db = DriverFactory(it)
        }
        return ::db.isInitialized
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            SERIES_URI_CODE -> {
                val query = db.createSeriesQuery(
                    projection = projection,
                    selection = selection,
                    selectionArgs = selectionArgs,
                    sortOrder = sortOrder
                )

                database.query(query)
            }
            EPISODES_URI_CODE -> {
                val query = db.createEpisodeQuery(
                    projection = projection,
                    selection = selection,
                    selectionArgs = selectionArgs,
                    sortOrder = sortOrder
                )

                database.query(query)
            }
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            SERIES_URI_CODE -> "vnd.android.cursor.dir/series"
            EPISODES_URI_CODE -> "vnd.android.cursor.dir/episodes"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    companion object {
        const val PROVIDER_NAME = "dev.datlag.burningseries.provider"
        const val SERIES_URI_CODE = 1337
        const val EPISODES_URI_CODE = 1338
    }
}