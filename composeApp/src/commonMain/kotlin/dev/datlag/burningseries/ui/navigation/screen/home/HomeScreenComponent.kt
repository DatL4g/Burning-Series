package dev.datlag.burningseries.ui.navigation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import dev.chrisbanes.haze.HazeState
import dev.datlag.burningseries.LocalHaze
import org.kodein.di.DI

class HomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI
): HomeComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        val haze = remember { HazeState() }

        CompositionLocalProvider(
            LocalHaze provides haze
        ) {
            onRender {
                HomeScreen(this)
            }
        }
    }
}