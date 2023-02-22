package dev.datlag.burningseries.common

import javafx.application.Platform
import netscape.javascript.JSObject

fun JSObject.getMemberAsString(key: String): String? {
    val member = this.getMember(key) ?: return null
    return (member as? String)?.ifEmpty { null } ?: member.toString().ifEmpty {
        null
    }
}

private var isJavaFxStarted: Boolean = false

var isStarted: Boolean
    get() = isJavaFxStarted
    set(value) {
        isJavaFxStarted = value
    }