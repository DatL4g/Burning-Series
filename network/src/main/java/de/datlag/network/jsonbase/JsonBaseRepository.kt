package de.datlag.network.jsonbase

import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.model.burningseries.series.HosterData
import de.datlag.model.common.toMD5
import de.datlag.model.jsonbase.BsHoster
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@Obfuscate
class JsonBaseRepository @Inject constructor(
    private val service: JsonBase
) {

    fun getBsHosterData(hosterData: HosterData): Flow<Resource<BsHoster>> = getBsHosterData(hosterData.href.toMD5())

    fun getBsHosterData(id: String): Flow<Resource<BsHoster>> = networkResource(
        fetchFromRemote = {
            service.getBsHosterData(id)
        }
    ).flowOn(Dispatchers.IO)
}