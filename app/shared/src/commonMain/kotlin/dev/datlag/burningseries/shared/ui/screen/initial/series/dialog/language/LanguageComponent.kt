package dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.language

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent

interface LanguageComponent : DialogComponent {

    val defaultLanguage: Series.Language
    val languages: List<Series.Language>

    fun onConfirm(language: Series.Language)
}