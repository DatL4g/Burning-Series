package dev.datlag.burningseries.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.ui.navigation.Component

class LoginScreenComponent(
    private val componentContext: ComponentContext,
    private val onLoginClicked: (user: String, password: String) -> Unit
) : Component, ComponentContext by componentContext {

    private var state by mutableStateOf(LoginData())

    @Composable
    override fun render() {
        LoginScreen(
            user = state.user,
            password = state.password,
            onUserChanged = {
                state = state.copy(user = it)
            },
            onPasswordChanged = {
                state = state.copy(password = it)
            },
            onLoginClicked = onLoginClicked
        )
    }

    private data class LoginData(
        val user: String = "",
        val password: String = ""
    )
}