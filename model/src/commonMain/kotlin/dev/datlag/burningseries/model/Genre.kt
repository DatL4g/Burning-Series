package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Genre(
    @SerialName("genre") val title: String,
    @SerialName("items") val items: List<Item>
) : Parcelable {

    @Parcelize
    @Serializable
    data class Item(
        @SerialName("title") val title: String,
        @SerialName("href") val href: String
    ) : Parcelable
}