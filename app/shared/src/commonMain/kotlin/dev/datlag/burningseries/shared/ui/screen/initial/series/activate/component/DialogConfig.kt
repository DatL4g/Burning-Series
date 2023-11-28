package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.component

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Stream

@Parcelize
sealed class DialogConfig : Parcelable {

    @Parcelize
    data class Success(val stream: Stream?) : DialogConfig(), Parcelable

    @Parcelize
    data object Error : DialogConfig(), Parcelable
}