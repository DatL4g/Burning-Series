package dev.datlag.burningseries.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.defaultComponentContext
import dev.datlag.burningseries.App
import dev.datlag.burningseries.runOnMainThreadBlocking
import dev.datlag.burningseries.ui.navigation.NavHostComponent

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = runOnMainThreadBlocking { NavHostComponent(defaultComponentContext()) }

        setContent {
            App {
                root.render()
            }
        }
    }
}