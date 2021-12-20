package de.datlag.burningseries.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.model.jsonbase.Stream
import de.datlag.network.video.VideoRepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class VideoViewModel @Inject constructor(
    val repository: VideoRepository
) : ViewModel() {

    fun getVideoSources(list: List<Stream>) = repository.getVideoSources(list)

    val videoSourcePos: MutableStateFlow<Int> = MutableStateFlow(0)
}