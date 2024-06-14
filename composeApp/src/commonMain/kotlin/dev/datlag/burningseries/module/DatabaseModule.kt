package dev.datlag.burningseries.module

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import dev.datlag.burningseries.database.DriverFactory
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.Episode
import dev.datlag.burningseries.database.InstantAdapter
import dev.datlag.burningseries.database.IntAdapter
import dev.datlag.burningseries.database.Series

data object DatabaseModule {

    const val NAME = "DatabaseModule"

    val di = DI.Module(NAME) {
        import(PlatformModule.di)

        bindSingleton<SqlDriver> {
            instance<DriverFactory>().createBurningSeriesDriver()
        }
        bindSingleton<BurningSeries> {
            BurningSeries(
                driver = instance<SqlDriver>(),
                SeriesAdapter = Series.Adapter(
                    seasonAdapter = IntAdapter,
                    seasonsAdapter = object : ColumnAdapter<List<Int>, String> {
                        override fun decode(databaseValue: String): List<Int> {
                            return if (databaseValue.isBlank()) {
                                emptyList()
                            } else {
                                databaseValue.split(',').mapNotNull {
                                    it.trim().toIntOrNull()
                                }
                            }
                        }

                        override fun encode(value: List<Int>): String {
                            return value.joinToString(separator = ",")
                        }
                    }
                ),
                EpisodeAdapter = Episode.Adapter(
                    numberAdapter = IntAdapter,
                    updatedAtAdapter = InstantAdapter
                )
            )
        }
    }
}