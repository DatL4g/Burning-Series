package dev.datlag.burningseries.ui.dialog.language

import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.network.repository.SeriesRepository
import kotlinx.coroutines.flow.map
import org.kodein.di.DI
import org.kodein.di.instance

class LanguageDialogComponent(
    componentContext: ComponentContext,
    private val onDismissed: () -> Unit,
    override val di: DI
) : LanguageComponent, ComponentContext by componentContext {

    private val seriesRepo: SeriesRepository by di.instance()
    override val languages = seriesRepo.series.map { it?.languages }
    override val selectedLanguage = seriesRepo.series.map { it?.selectedLanguage }

    override fun onDismissClicked() {
        onDismissed()
    }
}