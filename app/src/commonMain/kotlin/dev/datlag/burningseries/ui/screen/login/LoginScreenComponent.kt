package dev.datlag.burningseries.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.datastore.preferences.UserSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

class LoginScreenComponent(
    private val componentContext: ComponentContext,
    private val onLogin: () -> Unit,
    override val di: DI
) : LoginComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    private val userDataStore: DataStore<UserSettings> by di.instance()

    override val username = MutableStateFlow(userDataStore.username.getValueBlocking(String()))
    override val password = MutableStateFlow(userDataStore.password.getValueBlocking(String()))

    init {
        scope.launch(Dispatchers.IO) {
            username.emitAll(userDataStore.username)
        }
        scope.launch(Dispatchers.IO) {
            password.emitAll(userDataStore.password)
        }
    }

    override fun onLoginClicked() {
        scope.launch(Dispatchers.IO) {
            val newUsername = username.value
            val newPassword = password.value
            val updated = userDataStore.updateBSAccount(
                username = newUsername,
                password = newPassword,
                showedLogin = true
            )
            if (
                updated.burningSeries.username == newUsername
                && updated.burningSeries.password == newPassword
            ) {
                withContext(CommonDispatcher.Main) {
                    onLogin()
                }
            }
        }
    }

    @Composable
    override fun render() {
        LoginScreen(this)
    }
}