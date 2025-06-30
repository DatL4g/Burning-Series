package dev.datlag.mimasu.extension

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import co.touchlab.kermit.Logger
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.mimasu.extension.provider.SearchManager
import dev.datlag.mimasu.extension.ui.theme.Font
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.toTypography
import dev.datlag.tooling.safeCast
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instanceOrNull
import kotlin.reflect.safeCast

class MainActivity : ComponentActivity() {

    private val di: DI?
        get() = applicationContext.safeCast<DIAware>()?.di
            ?: application.safeCast<DIAware>()?.di
            ?: DIAware::class.safeCast(applicationContext)?.di
            ?: DIAware::class.safeCast(application)?.di

    private val firebaseWrapper: FirebaseWrapper?
        get() = di?.let {
            val creator by it.instanceOrNull<FirebaseWrapper.Creator>()
            val instance = creator?.let { c ->
                (c as? FirebaseWrapper.Creator.Available)?.wrapper
            }
            if (instance != null) {
                return@let instance
            }

            val fallbackInstance by it.instanceOrNull<FirebaseWrapper>()
            fallbackInstance
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        fun exit(reason: String?) {
            reason?.let { Logger.e(messageString = it) }
            finishAffinity()
        }

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        val di = this.di ?: return exit("Could not find dependency injection.")
        initializeSearchManager(di)

        setContent {
            App(
                di = di,
                typography = Font.manrope.toTypography()
            )
        }
    }

    override fun onStart() {
        super.onStart()

        initializeSearchManager()
    }

    override fun onRestart() {
        super.onRestart()

        initializeSearchManager()
    }

    override fun onResume() {
        super.onResume()

        initializeSearchManager()
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycleScope.launchIO {
            firebaseWrapper?.auth?.signOut()
        }
    }

    private fun initializeSearchManager(di: DI? = this.di) {
        val dependencyInjection = di ?: return
        val searchManager by dependencyInjection.instanceOrNull<SearchManager>()

        searchManager?.let { lifecycleScope.launchIO {
            it.initialize()
        } }
    }

}