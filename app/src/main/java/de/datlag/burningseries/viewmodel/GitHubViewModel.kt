package de.datlag.burningseries.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.burningseries.BuildConfig
import de.datlag.model.burningseries.common.getDigitsOrNull
import de.datlag.model.github.Release
import de.datlag.network.github.GitHubRepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class GitHubViewModel @Inject constructor(
    private val repository: GitHubRepository
) : ViewModel() {

    var showedNewVersion: Boolean = false

    fun getLatestRelease(): Flow<Release?> = flow<Release?> {
        val installedVersion = BuildConfig.VERSION_NAME
        val installedVersionAsDigitString = installedVersion.getDigitsOrNull()
        val installedVersionAsNumber = installedVersionAsDigitString?.toIntOrNull()

        repository.getReleases().collect {
            if (it.isNotEmpty()) {
                val installedRelease = it.find { release ->
                    val tagAsDigitString = release.tagAsNumberString()
                    release.tagName.equals(installedVersion, true)
                        || (tagAsDigitString != null
                            && installedVersionAsDigitString != null
                            && tagAsDigitString.equals(installedVersionAsDigitString, true)
                            )
                } ?: it.find { release ->
                    val tagAsNumber = release.tagAsNumberString()?.toIntOrNull()
                    tagAsNumber != null &&installedVersionAsNumber != null && tagAsNumber == installedVersionAsNumber
                }

                if (installedRelease != null) {
                    val newRelease = if (installedRelease.isPreRelease) {
                        checkNewReleaseAvailable(installedRelease, it)
                    } else {
                        val updateList = it.toMutableList().filterNot { release -> release.isPreRelease }
                        checkNewReleaseAvailable(installedRelease, updateList)
                    }
                    emit(newRelease)
                } else {
                    emit(null)
                }
            } else {
                emit(null)
            }
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    private fun checkNewReleaseAvailable(current: Release, list: List<Release>): Release? {
        return if (list.isNotEmpty()) {
            val installedEpoch = current.publishedAtSeconds

            val dateList = list.toMutableList().filter { release ->
                release.publishedAtSeconds > 0L && release.publishedAtSeconds > installedEpoch
            }

            if (installedEpoch == 0L || dateList.isEmpty()) {
                val installedTagNumber: Int? = current.tagAsNumberString()?.toIntOrNull()
                val tagNumberList = list.toMutableList().map { Pair(it, it.tagAsNumberString()?.toIntOrNull()) }.filterNot { it.second == null }
                if (installedTagNumber != null && tagNumberList.isNotEmpty()) {
                    val highestRelease = tagNumberList.maxByOrNull { it.second ?: 0 }
                    if ((highestRelease?.second ?: 0) > installedTagNumber) {
                        highestRelease?.first
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                dateList.maxByOrNull { it.publishedAtSeconds }
            }
        } else {
            null
        }
    }
}