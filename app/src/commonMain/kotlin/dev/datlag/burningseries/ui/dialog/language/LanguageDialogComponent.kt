package dev.datlag.burningseries.ui.dialog.language

import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.repository.SeriesRepository
import kotlinx.coroutines.flow.map
import org.kodein.di.DI
import org.kodein.di.instance

class LanguageDialogComponent(
    componentContext: ComponentContext,
    override val languages: List<Series.Language>,
    override val selectedLanguage: String,
    private val onDismissed: () -> Unit,
    private val onSelected: (Series.Language) -> Unit,
    override val di: DI
) : LanguageComponent, ComponentContext by componentContext {

    override fun onDismissClicked() {
        onDismissed()
    }

    override fun onConfirmNewLanguage(language: Series.Language) {
        onSelected(language)
    }
}