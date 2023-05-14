package dev.datlag.burningseries.ui.dialog.subtitle

import dev.datlag.burningseries.model.Language
import dev.datlag.burningseries.ui.dialog.DialogComponent

interface SubtitleComponent : DialogComponent {

    val subtitles: List<Language>
    val selectedLanguage: Language?

    fun onConfirmSubtitle(language: Language?)
}