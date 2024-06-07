package dev.datlag.burningseries.firebase

import android.content.Context
import android.util.Log
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

fun FirebaseFactory.Companion.initialize(
    context: Context,
    projectId: String?,
    applicationId: String,
    apiKey: String,
    localLogger: FirebaseFactory.Crashlytics.LocalLogger?
): FirebaseFactory {
    return CommonFirebase(
        app = Firebase.initialize(
            context = context,
            options = FirebaseOptions(
                projectId = projectId,
                applicationId = applicationId,
                apiKey = apiKey
            )
        ),
        localLogger = localLogger ?: object : FirebaseFactory.Crashlytics.LocalLogger {
            override fun warn(message: String?) {
                message?.let { Log.w(null, it) }
            }

            override fun error(throwable: Throwable?) {
                throwable?.let { Log.e(null, null, throwable) }
            }

            override fun error(message: String?) {
                message?.let { Log.e(null, it) }
            }
        }
    )
}