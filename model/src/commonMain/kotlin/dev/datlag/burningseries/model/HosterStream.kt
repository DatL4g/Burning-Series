package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class HosterStream(
    @SerialName("hoster") val hoster: String,
    @SerialName("url") val url: String,
    @SerialName("config") val config: Config = Config()
) : Parcelable {

    @Parcelize
    @Serializable
    data class Config(
        @SerialName("throwback") val recap: Info = Info(),
        @SerialName("intro") val intro: Info = Info(),
        @SerialName("outro") val outro: Info = Info()
    ) : Parcelable {

        @Parcelize
        @Serializable
        data class Info(
            @SerialName("start") val start: Long? = null,
            @SerialName("end") val end: Long? = null
        ) : Parcelable
    }
}
