package dev.datlag.mimasu.extension.matcher

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class MatchResult<T : Any>(
    val similarity: Double,
    @Contextual val data: T
)
