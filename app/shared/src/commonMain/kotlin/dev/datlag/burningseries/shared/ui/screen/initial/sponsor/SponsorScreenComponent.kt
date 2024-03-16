package dev.datlag.burningseries.shared.ui.screen.initial.sponsor


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.other.Crashlytics
import org.kodein.di.DI

class SponsorScreenComponent(
    componentContext: ComponentContext,
    override val di: DI
) : SponsorComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            SponsorScreen(this)
        }
        SideEffect {
            Crashlytics.screen(this)
        }
    }
}