package dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.shared.LocalDI
import org.kodein.di.DI

class LanguageDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val defaultLanguage: Series.Language,
    override val languages: List<Series.Language>,
    private val onDismissed: () -> Unit,
    private val onSelected: (Series.Language) -> Unit
) : LanguageComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            LanguageDialog(this)
        }
    }

    override fun dismiss() {
        onDismissed()
    }

    override fun onConfirm(language: Series.Language) {
        onSelected(language)
        dismiss()
    }
}