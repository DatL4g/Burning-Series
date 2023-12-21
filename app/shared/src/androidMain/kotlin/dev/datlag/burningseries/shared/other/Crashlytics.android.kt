package dev.datlag.burningseries.shared.other

import dev.datlag.burningseries.model.common.name
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

actual object Crashlytics {
    actual fun customKey(key: String, value: String) {
        Firebase.crashlytics.setCustomKey(key, value)
    }

    actual fun customKey(key: String, value: Boolean) {
        Firebase.crashlytics.setCustomKey(key, value)
    }
    actual fun customKey(key: String, value: Int) {
        Firebase.crashlytics.setCustomKey(key, value)
    }
    actual fun customKey(key: String, value: Long) {
        Firebase.crashlytics.setCustomKey(key, value)
    }
    actual fun customKey(key: String, value: Float) {
        Firebase.crashlytics.setCustomKey(key, value)
    }
    actual fun customKey(key: String, value: Double) {
        Firebase.crashlytics.setCustomKey(key, value)
    }

    actual fun screen(value: Component) {
        customKey("Screen", value::class.name)
    }
}