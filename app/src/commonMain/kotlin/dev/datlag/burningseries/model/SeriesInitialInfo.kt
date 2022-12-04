package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Parcelize
data class SeriesInitialInfo(
    val title: String,
    val cover: Cover?
) : Parcelable
