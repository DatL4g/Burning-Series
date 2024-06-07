package dev.datlag.burningseries.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.auth.auth

open class CommonFirebase(
    private val app: FirebaseApp,
    localLogger: FirebaseFactory.Crashlytics.LocalLogger?
) : FirebaseFactory {

    override val auth: FirebaseFactory.Auth = Auth(app)
    override val crashlytics: FirebaseFactory.Crashlytics = Crashlytics(app, localLogger)

    data class Auth(
        private val app: FirebaseApp
    ) : FirebaseFactory.Auth {

        override val isSignedIn: Boolean
            get() = Firebase.auth(app).currentUser != null

        override suspend fun signOut() {
            Firebase.auth(app).signOut()
        }
    }

    data class Crashlytics(
        private val app: FirebaseApp,
        override val localLogger: FirebaseFactory.Crashlytics.LocalLogger?
    ) : FirebaseFactory.Crashlytics {
        override fun customKey(key: String, value: String) {
            crashlyticsCustomKey(app, key, value)
        }
        override fun customKey(key: String, value: Boolean) {
            crashlyticsCustomKey(app, key, value)
        }
        override fun customKey(key: String, value: Int) {
            crashlyticsCustomKey(app, key, value)
        }
        override fun customKey(key: String, value: Long) {
            crashlyticsCustomKey(app, key, value)
        }
        override fun customKey(key: String, value: Float) {
            crashlyticsCustomKey(app, key, value)
        }
        override fun customKey(key: String, value: Double) {
            crashlyticsCustomKey(app, key, value)
        }
        override fun log(throwable: Throwable?) {
            localLogger?.error(throwable)
            crashlyticsLog(app, throwable)
        }
        override fun log(message: String?) {
            localLogger?.warn(message)
            crashlyticsLog(app, message)
        }
    }
}