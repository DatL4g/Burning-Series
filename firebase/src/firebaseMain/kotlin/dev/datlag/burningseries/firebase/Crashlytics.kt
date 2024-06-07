package dev.datlag.burningseries.firebase

import dev.gitlive.firebase.FirebaseApp

internal expect fun crashlyticsCustomKey(app: FirebaseApp, key: String, value: String)
internal expect fun crashlyticsCustomKey(app: FirebaseApp, key: String, value: Boolean)
internal expect fun crashlyticsCustomKey(app: FirebaseApp, key: String, value: Int)
internal expect fun crashlyticsCustomKey(app: FirebaseApp, key: String, value: Long)
internal expect fun crashlyticsCustomKey(app: FirebaseApp, key: String, value: Float)
internal expect fun crashlyticsCustomKey(app: FirebaseApp, key: String, value: Double)
internal expect fun crashlyticsLog(app: FirebaseApp, throwable: Throwable?)
internal expect fun crashlyticsLog(app: FirebaseApp, message: String?)