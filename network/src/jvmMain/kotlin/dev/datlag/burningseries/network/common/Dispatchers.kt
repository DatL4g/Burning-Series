package dev.datlag.burningseries.network.common

import kotlinx.coroutines.CoroutineDispatcher

actual object Dispatchers {
    actual val IO: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
}