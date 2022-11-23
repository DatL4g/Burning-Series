package dev.datlag.burningseries

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

actual inline fun <T : Any> runOnMainThreadBlocking(crossinline block: () -> T): T {
    return runBlocking(Dispatchers.Main) {
        block()
    }
}