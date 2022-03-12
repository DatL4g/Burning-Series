package de.datlag.burningseries.model

import kotlinx.serialization.Serializable

@Serializable
data class HostOptionalInfo(
    val isTv: Boolean = false
)
