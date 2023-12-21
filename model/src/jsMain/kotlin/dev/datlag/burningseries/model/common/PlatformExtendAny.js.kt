package dev.datlag.burningseries.model.common

import kotlin.reflect.KClass

actual val KClass<*>.name: String
    get() = this.simpleName?.ifBlank { null } ?: this.js.name.ifBlank { null } ?: this.toString()