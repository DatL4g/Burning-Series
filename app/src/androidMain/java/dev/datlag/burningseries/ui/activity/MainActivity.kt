package dev.datlag.burningseries.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.defaultComponentContext
import dev.datlag.burningseries.App
import dev.datlag.burningseries.LocalResources
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.module.DataStoreModule
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.module.PlatformModule
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
            CompositionLocalProvider(LocalResources provides resources, LocalStringRes provides stringRes) {
                App {
                    root.render()
                }
            }
        }
    }
}