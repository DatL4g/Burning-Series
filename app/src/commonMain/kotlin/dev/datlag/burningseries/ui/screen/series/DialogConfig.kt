package dev.datlag.burningseries.ui.screen.series

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Parcelize
sealed class DialogConfig : Parcelable {
    object Language : DialogConfig()
}