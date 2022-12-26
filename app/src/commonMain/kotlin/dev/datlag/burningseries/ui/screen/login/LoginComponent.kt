package dev.datlag.burningseries.ui.screen.login

import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface LoginComponent : Component {

    val username: MutableStateFlow<String>
    val password: MutableStateFlow<String>
    val isErroneous: MutableStateFlow<Boolean>

    val onSkip: () -> Unit

    fun onLoginClicked()
}