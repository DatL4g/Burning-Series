package de.datlag.burningseries.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.network.adblock.AdBlockRepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class AdBlockViewModel @Inject constructor(
    val repository: AdBlockRepository
) : ViewModel() {
    val adBlockList: MutableSharedFlow<Set<String>> = MutableSharedFlow()

    fun loadAdBlockList(fallbackStream: InputStream) = viewModelScope.launch(Dispatchers.IO) {
        val defaultList = readInputStream(fallbackStream)
        repository.getAdBlockList().collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    val combinedList: MutableSet<String> = mutableSetOf()
                    combinedList.addAll(defaultList)
                    val fetchedList = it.data?.let { stream -> readInputStream(stream) } ?: setOf()
                    combinedList.addAll(fetchedList)
                    adBlockList.emit(combinedList)
                }
                else -> adBlockList.emit(defaultList)
            }
        }
    }

    private suspend fun readInputStream(stream: InputStream): Set<String> {
        val result: MutableSet<String> = mutableSetOf()
        val inputReader = InputStreamReader(stream)
        val bufferedReader = BufferedReader(inputReader)
        var line: String
        try {
            while (bufferedReader.readLine().also { line = it } != null) {
                if (line.trim().isNotEmpty()) {
                    result.add(line)
                }
            }
        } catch (ignored: Exception) { }
        return result
    }
}