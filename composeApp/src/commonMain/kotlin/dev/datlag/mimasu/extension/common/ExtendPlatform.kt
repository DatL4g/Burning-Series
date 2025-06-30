package dev.datlag.mimasu.extension.common

import kotlin.reflect.KClass

expect infix fun <T : Any> KClass<T>.typeOf(base: KClass<*>): Boolean