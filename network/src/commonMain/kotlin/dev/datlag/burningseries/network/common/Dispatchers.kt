package dev.datlag.burningseries.network.common

import kotlinx.coroutines.CoroutineDispatcher

expect object Dispatchers {
    val IO: CoroutineDispatcher
}