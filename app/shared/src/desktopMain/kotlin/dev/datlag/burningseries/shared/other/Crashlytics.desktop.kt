package dev.datlag.burningseries.shared.other

import dev.datlag.burningseries.model.common.name
import dev.datlag.burningseries.shared.ui.navigation.Component

actual object Crashlytics {
    actual fun customKey(key: String, value: String) { }
    actual fun customKey(key: String, value: Boolean) { }
    actual fun customKey(key: String, value: Int) { }
    actual fun customKey(key: String, value: Long) { }
    actual fun customKey(key: String, value: Float) { }
    actual fun customKey(key: String, value: Double) { }
    actual fun screen(value: Component) {
        customKey("Screen", value::class.name)
    }
}