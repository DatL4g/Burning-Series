package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class InsertStream(
    @SerialName("inserted") val inserted: Long,
    @SerialName("updated") val updated: Long,
    @SerialName("failed") val failed: Long
) : Parcelable
