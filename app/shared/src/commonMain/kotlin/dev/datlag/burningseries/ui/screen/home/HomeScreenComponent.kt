package dev.datlag.burningseries.ui.screen.home

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI

class HomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI
) : HomeComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        HomeScreen(this)
    }
}