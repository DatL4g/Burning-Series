package dev.datlag.mimasu.extension

import android.content.Context
import androidx.multidex.MultiDexApplication
import dev.datlag.mimasu.extension.module.NetworkModule
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton

class App : MultiDexApplication(), DIAware {

    override val di: DI = DI {
        bindSingleton<Context> {
            applicationContext
        }

        import(NetworkModule.di)
    }

    override fun onCreate() {
        super.onCreate()

        if (AppInitializer.isSekretLoaded(applicationContext)) {
            val appId = Sekret.firebaseAppId(BuildConfig.APPLICATION_ID)
            val apiKey = Sekret.firebaseApiKey(BuildConfig.APPLICATION_ID)

            if (appId.isNullOrBlank() || apiKey.isNullOrBlank()) {
                return
            }

            Firebase.initialize(
                context = this,
                options = FirebaseOptions(
                    projectId = Sekret.projectId(BuildConfig.APPLICATION_ID),
                    applicationId = appId,
                    apiKey = apiKey
                )
            )
        }
    }
}