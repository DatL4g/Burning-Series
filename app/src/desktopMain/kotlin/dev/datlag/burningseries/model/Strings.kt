package dev.datlag.burningseries.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("resources", "", "")
data class XMLResources(
    val strings: List<XMLString>
)

@Serializable
@XmlSerialName("string", "", "")
data class XMLString(
    val name: String,
    val translatable: Boolean = true,
    @XmlValue(true) val data: String
)