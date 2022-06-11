package de.datlag.burningseries.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.model.video.ScrapeHoster
import de.datlag.network.burningseries.BurningSeriesRepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class ScrapeHosterViewModel @Inject constructor(
    val repository: BurningSeriesRepository,
    val jsonBuilder: Json
) : ViewModel() {
    private val streamSet: MutableSet<ScrapeHoster> = mutableSetOf()

    fun saveIfNotPresent(callbackValue: String) = flow {
        val scraped = jsonBuilder.decodeFromString<ScrapeHoster>(callbackValue)
        if (!streamSet.contains(scraped)) {
            streamSet.add(scraped)
            emitAll(repository.saveScrapedHoster(scraped))
        }
    }.flowOn(Dispatchers.IO)
}