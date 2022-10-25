package de.datlag.network.video

import androidx.datastore.core.DataStore
import de.datlag.datastore.SettingsPreferences
import de.datlag.model.burningseries.stream.Stream
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@Obfuscate
class VideoRepository @Inject constructor(
    val scraper: VideoScraper
) {

    @Inject
    lateinit var settingsDataStore: DataStore<SettingsPreferences>

    fun getVideoSources(list: List<Stream>) = settingsDataStore.data.map { it.video.preferMp4 }.transform { data ->
        return@transform emitAll(combine(list.map { scraper.scrapeVideosFrom(it, data) }) { array ->
            array.filterNotNull().sortedBy { it.hoster }
        })
    }
}