package dev.datlag.burningseries.ui.screen.home.episode

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.network.repository.HomeRepository
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class EpisodesViewComponent(
    componentContext: ComponentContext,
    override val di: DI
) : EpisodesComponent, ComponentContext by componentContext {

    private val homeRepo: HomeRepository by di.instance()
    override val status = homeRepo.status
    override val episodes = homeRepo.episodes

    @Composable
    override fun render() {
        EpisodesView(this)
    }
}