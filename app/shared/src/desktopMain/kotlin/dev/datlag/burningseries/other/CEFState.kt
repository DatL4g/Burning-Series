package dev.datlag.burningseries.other

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalCEFInitialization = staticCompositionLocalOf<MutableState<CEFState>> { error("No CEFInitialization state provided") }

sealed interface CEFState {
    data object LOCATING : CEFState
    data class Downloading(val progress: Float) : CEFState
    data object EXTRACTING : CEFState
    data object INSTALLING : CEFState
    data object INITIALIZING : CEFState
    data object INITIALIZED : CEFState
}