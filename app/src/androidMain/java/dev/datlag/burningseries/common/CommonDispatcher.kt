package dev.datlag.burningseries.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object CommonDispatcher {
    actual val IO: CoroutineDispatcher
        get() = Dispatchers.IO
    actual val Main: CoroutineDispatcher
        get() = Dispatchers.Main
}