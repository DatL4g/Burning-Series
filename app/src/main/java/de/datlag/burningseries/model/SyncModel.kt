package de.datlag.burningseries.model

import de.datlag.model.burningseries.series.relation.SeriesWithEpisode
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SyncModel { }

@Serializable
@SerialName("request")
data class SyncRequest(val amount: Int) : SyncModel()

@Serializable
@SerialName("requestAccept")
data class SyncRequestAccept(val amount: Int) : SyncModel()

@Serializable
@SerialName("sync")
data class Sync(val data: SeriesWithInfo) : SyncModel()