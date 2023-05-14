package dev.datlag.burningseries.ui.screen.video

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Language

@Parcelize
sealed class DialogConfig : Parcelable {
    data class Subtitle(
        val languages: List<Language>,
        val selectedLanguage: Language?
    ) : DialogConfig()
}
