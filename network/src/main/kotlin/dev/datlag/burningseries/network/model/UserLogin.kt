package dev.datlag.burningseries.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserLogin(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)
