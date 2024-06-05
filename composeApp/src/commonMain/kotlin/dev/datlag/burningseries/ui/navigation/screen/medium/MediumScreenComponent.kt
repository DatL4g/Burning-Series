package dev.datlag.burningseries.ui.navigation.screen.medium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import dev.chrisbanes.haze.HazeState
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.model.SeriesData
import org.kodein.di.DI

class MediumScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val initialSeriesData: SeriesData
) : MediumComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        val haze = remember { HazeState() }

        CompositionLocalProvider(
            LocalHaze provides haze
        ) {
            onRender {
                MediumScreen(this)
            }
        }
    }
}