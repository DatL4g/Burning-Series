package de.datlag.burningseries.viewmodel

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.datastore.SettingsPreferences
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class SettingsViewModel @Inject constructor(
    val dataStore: DataStore<SettingsPreferences>
) : ViewModel() {

    val data: Flow<SettingsPreferences> = dataStore.data.flowOn(Dispatchers.IO).distinctUntilChanged()

    fun updateAppearanceDarkMode(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.toBuilder().setAppearance(it.appearance.toBuilder().setDarkMode(newValue).build()).build()
        }
    }

    fun updateAppearanceImproveDialog(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.toBuilder().setAppearance(it.appearance.toBuilder().setImproveDialog(newValue).build()).build()
        }
    }

    fun updateVideoAdvancedFetching(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.toBuilder().setVideo(it.video.toBuilder().setAdvancedFetching(newValue).build()).build()
        }
    }

    fun updateVideoPreferMp4(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.toBuilder().setVideo(it.video.toBuilder().setPreferMp4(newValue).build()).build()
        }
    }

    fun updateVideoPreview(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.toBuilder().setVideo(it.video.toBuilder().setPreviewEnabled(newValue).build()).build()
        }
    }

    fun updateVideoFullscreen(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.toBuilder().setVideo(it.video.toBuilder().setDefaultFullscreen(newValue).build()).build()
        }
    }

    fun updateUserMalAuth(newValue: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.toBuilder().setUser(it.user.toBuilder().setMalAuth(newValue).build()).build()
        }
    }

    fun updateUserMalImages(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val aniListImagesValue = if (newValue) {
            false
        } else {
            dataStore.data.map { it.user.aniListImages }.first()
        }
        dataStore.updateData {
            it.toBuilder().setUser(it.user.toBuilder().setMalImages(newValue).setAniListImages(aniListImagesValue).build()).build()
        }
    }

    fun updateUserAniListAuth(newValue: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.updateData {
            it.toBuilder().setUser(it.user.toBuilder().setAnilistAuth(newValue).build()).build()
        }
    }

    fun updateUserAniListImages(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val malImagesValue = if (newValue) {
            false
        } else {
            dataStore.data.map { it.user.malImages }.first()
        }
        dataStore.updateData {
            it.toBuilder().setUser(it.user.toBuilder().setAniListImages(newValue).setMalImages(malImagesValue).build()).build()
        }
    }
}