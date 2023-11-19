package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Parcelize
data class Stream(
    val list: List<String>,
    val headers: Map<String, String>
) : Parcelable
