package dev.datlag.burningseries.ui.screen.series

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Series

@Parcelize
sealed class DialogConfig : Parcelable {
    object Language : DialogConfig()
    object Season : DialogConfig()

    data class NoStream(val episode: Series.Episode) : DialogConfig()
}