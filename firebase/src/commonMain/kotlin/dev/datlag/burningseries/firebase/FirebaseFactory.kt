package dev.datlag.burningseries.firebase

interface FirebaseFactory {

    val auth: Auth
    val crashlytics: Crashlytics
    val store: Store

    data object Empty : FirebaseFactory, Auth, Crashlytics, Store {
        override val auth: Auth = this
        override val crashlytics: Crashlytics = this
        override val store: Store = this
    }

    interface Auth {
        val isSignedIn: Boolean
            get() = false

        suspend fun signInAnonymously(): Boolean = false
        suspend fun signOut() { }

        suspend fun delete() { }

        companion object
    }

    interface Store {
        suspend fun streams(hrefList: List<String>): List<String> = emptyList()

        companion object
    }

    interface Crashlytics {
        val localLogger: LocalLogger?
            get() = null

        fun customKey(key: String, value: String) { }
        fun customKey(key: String, value: Boolean) { }
        fun customKey(key: String, value: Int) { }
        fun customKey(key: String, value: Long) { }
        fun customKey(key: String, value: Float) { }
        fun customKey(key: String, value: Double) { }
        fun log(throwable: Throwable?) { }
        fun log(message: String?) { }

        interface LocalLogger {
            fun warn(message: String?)
            fun error(throwable: Throwable?)
            fun error(message: String?)
        }

        companion object
    }

    companion object
}