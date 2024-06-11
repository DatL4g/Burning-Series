package dev.datlag.burningseries.module

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import dev.datlag.burningseries.database.DriverFactory
import dev.datlag.burningseries.database.BurningSeries
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
                    seasonAdapter = object : ColumnAdapter<Int, Long> {
                        override fun decode(databaseValue: Long): Int {
                            return databaseValue.toInt()
                        }

                        override fun encode(value: Int): Long {
                            return value.toLong()
                        }
                    }
                )
            )
        }
    }
}