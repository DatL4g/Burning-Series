package dev.datlag.burningseries.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.crashlytics.crashlytics

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: String
) {
    Firebase.crashlytics(app).setCustomKey(key, value)
}

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Boolean
) {
    Firebase.crashlytics(app).setCustomKey(key, value)
}

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Int
) {
    Firebase.crashlytics(app).setCustomKey(key, value)
}

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Long
) {
    Firebase.crashlytics(app).setCustomKey(key, value)
}

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Float
) {
    Firebase.crashlytics(app).setCustomKey(key, value)
}

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Double
) {
    Firebase.crashlytics(app).setCustomKey(key, value)
}

internal actual fun crashlyticsLog(app: FirebaseApp, throwable: Throwable?) {
    throwable?.let {
        Firebase.crashlytics(app).recordException(it)
    }
}

internal actual fun crashlyticsLog(app: FirebaseApp, message: String?) {
    message?.ifBlank { null }?.let {
        Firebase.crashlytics(app).log(it)
    }
}