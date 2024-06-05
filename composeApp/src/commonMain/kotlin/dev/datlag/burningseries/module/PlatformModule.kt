package dev.datlag.burningseries.module

import coil3.ImageLoader
import org.kodein.di.DI

expect object PlatformModule {
    val di: DI.Module
}

expect fun ImageLoader.Builder.extendImageLoader(): ImageLoader.Builder