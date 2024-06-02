package dev.datlag.burningseries.module

import org.kodein.di.DI

data object NetworkModule {

    const val NAME = "NetworkModule"

    val di = DI.Module(NAME) {
        import(PlatformModule.di)
    }
}