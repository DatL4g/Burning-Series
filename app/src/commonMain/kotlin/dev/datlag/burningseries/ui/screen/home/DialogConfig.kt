package dev.datlag.burningseries.ui.screen.home

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Release

@Parcelize
sealed class DialogConfig : Parcelable {
    data class NewRelease(val release: Release) : DialogConfig()
}
