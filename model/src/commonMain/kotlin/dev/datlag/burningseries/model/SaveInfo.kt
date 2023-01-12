package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Parcelize
data class SaveInfo(
    val success: Boolean,
    val href: String,
    val stream: VideoStream?
) : Parcelable
