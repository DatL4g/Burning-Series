package dev.datlag.burningseries.ui.dialog.language

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.dialog.DialogComponent
import kotlinx.coroutines.flow.Flow

interface LanguageComponent : DialogComponent {

    val languages: List<Series.Language>
    val selectedLanguage: String?

    fun onConfirmNewLanguage(language: Series.Language)
}