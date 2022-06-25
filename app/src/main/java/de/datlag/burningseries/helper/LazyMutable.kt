package de.datlag.burningseries.helper

import io.michaelrocks.paranoid.Obfuscate
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Obfuscate
class LazyMutable<T>(
    val initializer: () -> T
): ReadWriteProperty<Any?, T> {
    private val lazyValue by lazy { initializer() }
    private var newValue: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = newValue ?: lazyValue

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        newValue = value
    }
}

fun <T> lazyMutable(initializer: () -> T) = LazyMutable(initializer)