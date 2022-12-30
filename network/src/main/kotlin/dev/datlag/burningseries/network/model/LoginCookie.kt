package dev.datlag.burningseries.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginCookie(
    @SerialName("name") val name: String,
    @SerialName("value") val value: String,
    @SerialName("maxAge") val maxAge: Long,
    @SerialName("expires") val expires: Long
)


@Serializable
data class LoginInfo(
    @SerialName("loginCookie") val loginCookie: LoginCookie,
    @SerialName("uidCookie") val uidCookie: LoginCookie
)