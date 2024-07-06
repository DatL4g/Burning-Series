package dev.datlag.burningseries.firebase

import dev.gitlive.firebase.FirebaseApp

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: String
) { }

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Boolean
) { }

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Int
) { }

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Long
) { }

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Float
) { }

internal actual fun crashlyticsCustomKey(
    app: FirebaseApp,
    key: String,
    value: Double
) { }

internal actual fun crashlyticsLog(app: FirebaseApp, throwable: Throwable?) { }

internal actual fun crashlyticsLog(app: FirebaseApp, message: String?) { }