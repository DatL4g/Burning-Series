package dev.datlag.burningseries

import dev.datlag.burningseries.model.common.scopeCatching
import java.awt.Toolkit

object AppIO {

    fun applyTitle(title: String) = scopeCatching {
        val toolkit = Toolkit.getDefaultToolkit()
        val awtAppClassNameField = toolkit.javaClass.getDeclaredField("awtAppClassName")
        val working = try {
            awtAppClassNameField.isAccessible = true
            awtAppClassNameField.canAccess(null)
        } catch (ignored: Throwable) {
            awtAppClassNameField.trySetAccessible()
        }
        awtAppClassNameField.set(toolkit, title)
        working
    }.getOrNull() ?: false
}