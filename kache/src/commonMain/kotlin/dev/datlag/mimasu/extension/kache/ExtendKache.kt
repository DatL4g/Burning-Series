package dev.datlag.mimasu.extension.kache

import com.mayakapps.kache.ObjectKache
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.scopeCatching

fun <K : Any, V : Any> ObjectKache<K, V>.sync(key: K): V? {
    return scopeCatching {
        this.getIfAvailable(key)
    }.getOrNull()
}

suspend fun <K : Any, V : Any> ObjectKache<K, V>.async(key: K): V? {
    return sync(key) ?: suspendCatching {
        this@async.get(key)
    }.getOrNull()
}

suspend fun <K : Any, V : Any> ObjectKache<K, V>.async(key: K, creationFunction: suspend (key: K) -> V?): V? {
    this.async(key)?.let { return it }

    var created: V? = null
    return suspendCatching {
        this@async.put(key) { creationFunction.invoke(key).also { created = it } }
    }.getOrNull() ?: created
}