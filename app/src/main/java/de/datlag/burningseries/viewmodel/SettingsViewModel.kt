package de.datlag.burningseries.viewmodel

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.datastore.SettingsPreferences
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class SettingsViewModel @Inject constructor(
    val dataStore: DataStore<SettingsPreferences>
) : ViewModel() {

    val data: Flow<SettingsPreferences> = dataStore.data.flowOn(Dispatchers.IO)

    fun updateVideoAdvancedFetching(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.video
            it.toBuilder().setVideo(it.video.toBuilder().setAdvancedFetching(newValue).build()).build()
        }
    }

    fun updateVideoPreferMp4(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.toBuilder().setVideo(it.video.toBuilder().setPreferMp4(newValue).build()).build()
        }
    }
}