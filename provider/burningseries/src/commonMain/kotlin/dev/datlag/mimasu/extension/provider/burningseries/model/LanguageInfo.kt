package dev.datlag.mimasu.extension.provider.burningseries.model

import kotlinx.serialization.Serializable

@Serializable
data class LanguageInfo(
    val localeTitle: String,
    val locale: String
)
