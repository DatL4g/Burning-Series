package common

import org.w3c.dom.*
import kotlin.js.Promise

fun <T> Promise<T>?.collect(listener: (T) -> Unit) = this?.then(listener)

fun Any?.isNullOrEmpty(): Boolean = when (this) {
    null -> true
    is Collection<*> -> this.isNullOrEmpty()
    is Array<*> -> this.isEmpty()
    else -> {
        this == undefined || ((this is String || jsTypeOf(this).equals("string", true)) && (this.toString().isEmpty() || this.toString().isBlank()))
    }
}

fun HTMLCollection?.forEach(listener: (Element?) -> Unit) {
    if (this == null) {
        return
    }

    for (i in 0 until this.length) {
        listener(this[i])
    }
}

fun HTMLCollection?.forEachNotNull(listener: (Element) -> Unit) {
    if (this == null) {
        return
    }

    for (i in 0 until this.length) {
        this[i]?.let(listener)
    }
}

fun Document?.onReady(listener: () -> Unit) {
    if (this == null) {
        return
    }

    this.onreadystatechange = {
        if (this.readyState == DocumentReadyState.COMPLETE) {
            listener()
        }
    }
}

fun ByteArray.toHexString(): String {
    fun doubleDigit(value: String): String {
        return if (value.length == 1) {
            "0$value"
        } else {
            value
        }
    }

    val builder = StringBuilder()
    this.forEach {
        builder.append(doubleDigit((it.toInt() and 0xFF).toString(16)))
    }
    return builder.toString()
}