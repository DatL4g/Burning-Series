package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.model.InsertStream
import dev.datlag.burningseries.model.ScrapedHoster
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class SaveRepository(
    private val api: BurningSeries
) {

    private val _scrapedHoster: MutableStateFlow<ScrapedHoster?> = MutableStateFlow(null)
    private val _saveScrapedHoster: Flow<Resource<InsertStream>> = _scrapedHoster.debounce(500).distinctUntilChanged().transformLatest {
        if (it != null) {
            return@transformLatest emitAll(networkResource(
                makeNetworkRequest = {
                    api.save(it)
                }
            ).distinctUntilChanged())
        }
    }.flowOn(Dispatchers.IO)

    private val _status = _saveScrapedHoster.transformLatest {
        return@transformLatest emit(it.status)
    }.flowOn(Dispatchers.IO)

    val status = _status.transformLatest {
        return@transformLatest emit(Status.create(it, true))
    }.flowOn(Dispatchers.IO)

    val saveScrapedSuccess = _status.transformLatest {
        when (it) {
            is Resource.Status.Success -> return@transformLatest emit(it.data.failed <= 0)
            is Resource.Status.Error -> return@transformLatest emit(false)
            else -> { }
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)


    suspend fun save(scrapedHoster: ScrapedHoster) = withContext(Dispatchers.IO) {
        this@SaveRepository._scrapedHoster.emit(scrapedHoster)
    }
}