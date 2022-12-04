package dev.datlag.burningseries.ui.activity

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import com.arkivanov.decompose.defaultComponentContext
import dev.datlag.burningseries.App
import dev.datlag.burningseries.LocalOrientation
import dev.datlag.burningseries.LocalResources
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.module.DataStoreModule
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.module.PlatformModule
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.other.Resources
import dev.datlag.burningseries.other.StringRes
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import org.kodein.di.DI
import org.kodein.di.bindSingleton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val di = DI {
            bindSingleton {
                this@MainActivity.applicationContext
            }

            import(NetworkModule.di)
            import(DataStoreModule.di)
        }

        val root = NavHostComponent.create(defaultComponentContext(), di)
        val resources = Resources(assets)
        val stringRes = StringRes(this)

        setContent {
            val configuration = LocalConfiguration.current
            val orientation = when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
                else -> Orientation.PORTRAIT
            }

            CompositionLocalProvider(
                LocalResources provides resources,
                LocalStringRes provides stringRes,
                LocalOrientation provides orientation
            ) {
                App {
                    root.render()
                }
            }
        }
    }
}