package dev.datlag.burningseries

import java.awt.Toolkit

object AppIO {

    fun applyTitle(title: String) = runCatching {
        val toolkit = Toolkit.getDefaultToolkit()
        val awtAppClassNameField = toolkit.javaClass.getDeclaredField("awtAppClassName")
        val working = try {
            awtAppClassNameField.isAccessible = true
            awtAppClassNameField.isAccessible
        } catch (ignored: Throwable) {
            awtAppClassNameField.trySetAccessible()
        }
        awtAppClassNameField.set(toolkit, title)
        working
    }.getOrNull() ?: false
}