package dev.datlag.burningseries.ui.screen.initial.favorite

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI

class FavoriteScreenComponent(
    componentContext: ComponentContext,
    override val di: DI
) : FavoriteComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        FavoriteScreen(this)
    }
}