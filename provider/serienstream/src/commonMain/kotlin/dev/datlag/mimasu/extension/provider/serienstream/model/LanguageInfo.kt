package dev.datlag.mimasu.extension.provider.serienstream.model

import kotlinx.serialization.Serializable

@Serializable
data class LanguageInfo(
    val localeTitle: String,
    val locale: String
)