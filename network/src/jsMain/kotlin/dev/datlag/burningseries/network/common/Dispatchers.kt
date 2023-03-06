package dev.datlag.burningseries.network.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object Dispatchers {
    actual val IO: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default
}