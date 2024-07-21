package dev.datlag.burningseries.other

import kotlinx.coroutines.flow.MutableStateFlow

data object StateSaver {
    var sekretLibraryLoaded: Boolean = false
    val defaultHome = MutableStateFlow(false)
}