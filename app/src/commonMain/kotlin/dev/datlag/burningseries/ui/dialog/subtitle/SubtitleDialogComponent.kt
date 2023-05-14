package dev.datlag.burningseries.ui.dialog.subtitle

import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Language
import org.kodein.di.DI

class SubtitleDialogComponent(
    componentContext: ComponentContext,
    override val subtitles: List<Language>,
    override val selectedLanguage: Language?,
    private val onDismissed: () -> Unit,
    private val onSelected: (Language?) -> Unit,
    override val di: DI
) : SubtitleComponent, ComponentContext by componentContext {

    override fun onDismissClicked() {
        onDismissed()
    }

    override fun onConfirmSubtitle(language: Language?) {
        onSelected(language)
    }
}