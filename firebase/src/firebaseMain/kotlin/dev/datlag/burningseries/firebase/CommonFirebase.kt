package dev.datlag.burningseries.firebase

import dev.datlag.burningseries.model.HosterScraping
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where

open class CommonFirebase(
    private val app: FirebaseApp,
    localLogger: FirebaseFactory.Crashlytics.LocalLogger?
) : FirebaseFactory {

    override val auth: FirebaseFactory.Auth = Auth(app)
    override val crashlytics: FirebaseFactory.Crashlytics = Crashlytics(app, localLogger)
    override val store: FirebaseFactory.Store = Store(app)

    data class Auth(
        private val app: FirebaseApp
    ) : FirebaseFactory.Auth {

        override val isSignedIn: Boolean
            get() = Firebase.auth(app).currentUser != null

        override suspend fun signInAnonymously(): Boolean {
            return Firebase.auth(app).signInAnonymously().user != null
        }

        override suspend fun signOut() {
            Firebase.auth(app).signOut()
        }

        override suspend fun delete() {
            Firebase.auth(app).currentUser?.delete()

            signOut()
        }
    }

    data class Store(
        private val app: FirebaseApp
    ) : FirebaseFactory.Store {
        override suspend fun streams(hrefList: List<String>): List<String> {
            return Firebase.firestore(app).collection("stream").where {
                all(
                    *listOfNotNull(
                        hrefList.let { "id" inArray it }
                    ).toTypedArray()
                )
            }.get().documents.map {
                it.get<String>("url")
            }
        }

        override suspend fun addStream(data: HosterScraping.FireStore): Boolean {
            val fireStore = Firebase.firestore(app)
            val document = fireStore.collection("stream").where {
                "id" equalTo data.id
            }.get().documents.firstOrNull()?.reference?: fireStore.collection("stream").document

            fireStore.runTransaction {
                set(document, data = data, merge = true)
            }
            return true
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