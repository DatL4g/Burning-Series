package dev.datlag.burningseries.shared.other

import dev.datlag.burningseries.shared.ui.navigation.Component

expect object Crashlytics {
    fun customKey(key: String, value: String)
    fun customKey(key: String, value: Boolean)
    fun customKey(key: String, value: Int)
    fun customKey(key: String, value: Long)
    fun customKey(key: String, value: Float)
    fun customKey(key: String, value: Double)
    fun screen(value: Component)
}