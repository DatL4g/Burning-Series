package dev.datlag.mimasu.extension.common

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

actual infix fun <T : Any> KClass<T>.typeOf(base: KClass<*>): Boolean {
    return this == base || this.isSubclassOf(base)
}