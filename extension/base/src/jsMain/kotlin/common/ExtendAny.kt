package common

import kotlinx.dom.removeClass
import org.w3c.dom.*
import kotlin.js.Promise
import kotlin.collections.isNullOrEmpty as defaultNullOrEmpty

fun <T> Promise<T>?.collect(listener: (T) -> Unit) = this?.then(listener)

fun Any?.isNullOrEmpty(): Boolean = when (this) {
    null -> true
    is Collection<*> -> this.defaultNullOrEmpty()
    is Array<*> -> this.defaultNullOrEmpty()
    else -> {
        this == undefined || ((this is String || jsTypeOf(this).equals("string", true)) && (this.toString().isEmpty() || this.toString().isBlank()))
    }
}

inline fun HTMLCollection?.forEach(listener: (Element?) -> Unit) {
    if (this == null) {
        return
    }
    for (i in 0 until this.length) {
        listener(this[i])
    }
}

inline fun HTMLCollection?.forEachNotNull(listener: (Element) -> Unit) {
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

    if (this.readyState == DocumentReadyState.COMPLETE) {
        listener()
        return
    }

    this.onreadystatechange = {
        if (this.readyState == DocumentReadyState.COMPLETE) {
            listener()
        }
    }
}

fun Element?.removeAllClasses() {
    if (this == null) {
        return
    }

    val allClasses = this.classList
    for (i in 0 until allClasses.length) {
        this.removeClass(allClasses[i] ?: String())
    }
}

inline fun <I> objectOf(
    jsonObject: I = js("new Object()").unsafeCast<I>(),
    writer: I.() -> Unit
): I {
    writer(jsonObject)
    return jsonObject
}

fun Element.insertAfter(newNode: Node) {
    this.parentNode?.insertBefore(newNode, this.nextSibling)
}