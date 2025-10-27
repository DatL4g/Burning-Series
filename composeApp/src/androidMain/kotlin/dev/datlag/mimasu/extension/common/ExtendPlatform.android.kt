package dev.datlag.mimasu.extension.common

import dev.datlag.tooling.deleteSafely
import dev.datlag.tooling.existsSafely
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

actual infix fun <T : Any> KClass<T>.typeOf(base: KClass<*>): Boolean {
    return this == base || this.isSubclassOf(base)
}

fun File.deleteRecursivelySafely(): Boolean = walkBottomUp().fold(true) { res, it ->
    (it.deleteSafely(res) || !it.existsSafely(res)) && res
}