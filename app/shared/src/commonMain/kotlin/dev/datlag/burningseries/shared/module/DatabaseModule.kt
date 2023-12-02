package dev.datlag.burningseries.shared.module

import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.DriverFactory
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

data object DatabaseModule {

    const val NAME = "DatabaseModule"

    val di = DI.Module(NAME) {
        import(PlatformModule.di)

        bindSingleton("BurningSeriesDriver") {
            instance<DriverFactory>().createBurningSeriesDriver()
        }
        bindSingleton {
            BurningSeries(instance("BurningSeriesDriver"))
        }
    }
}