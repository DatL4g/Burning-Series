package dev.datlag.burningseries.ui.screen.initial.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI

class SeriesScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    val initialTitle: String,
    val initialHref: String,
    val initialCoverHref: String?
) : SeriesComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        Box(modifier = Modifier.fillMaxSize().background(Color.Red))
    }
}