package dev.datlag.burningseries.other

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data object PictureInPicture {
    val enabled = MutableStateFlow(false)
    val active = MutableStateFlow(false)

    val isEnabled: Boolean
        get() = enabled.value

    fun setEnabled(value: Boolean) = enabled.update { value }
    fun setActive(value: Boolean) = active.update { value }
}