package dev.datlag.burningseries.ui.screen.video

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Parcelize
sealed class DialogConfig : Parcelable {

    @Parcelize
    data class Subtitle(
        val list: List<VideoComponent.Subtitle>
    ) : DialogConfig(), Parcelable
}