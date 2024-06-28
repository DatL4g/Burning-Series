package dev.datlag.burningseries.k2k

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.properties.Delegates

@Serializable
data class Host(
    @SerialName("name") val name: String,
    @SerialName("filterMatch") val filterMatch: String = ""
) {
    @Transient
    lateinit var hostAddress: String

    var port by Delegates.notNull<Int>()
}
