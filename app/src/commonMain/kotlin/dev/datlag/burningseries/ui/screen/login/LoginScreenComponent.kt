package dev.datlag.burningseries.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.datastore.common.password
import dev.datlag.burningseries.datastore.common.updateBSAccount
import dev.datlag.burningseries.datastore.common.username
import dev.datlag.burningseries.datastore.preferences.UserSettings
import dev.datlag.burningseries.network.model.UserLogin
import dev.datlag.burningseries.network.repository.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class LoginScreenComponent(
    private val componentContext: ComponentContext,
    private val onLogin: () -> Unit,
    override val di: DI
) : LoginComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    private val userDataStore: DataStore<UserSettings> by di.instance()
    private val userRepo: UserRepository by di.instance()
    private val json: Json by di.instance()

    override val username = MutableStateFlow(userDataStore.username.getValueBlocking(String()))
    override val password = MutableStateFlow(userDataStore.password.getValueBlocking(String()))
    override val isErroneous = MutableStateFlow(false)

    init {
        scope.launch(Dispatchers.IO) {
            username.emitAll(userDataStore.username)
        }
        scope.launch(Dispatchers.IO) {
            password.emitAll(userDataStore.password)
        }
        scope.launch(Dispatchers.IO) {
            userDataStore.updateBSAccount(
                showedLogin = true
            )
        }
    }

    override fun onLoginClicked() {
        scope.launch(Dispatchers.IO) {
            userRepo.bsLogin(
                username.value,
                password.value,
                json.encodeToString(UserLogin(username.value, password.value)).encodeBase64()
            ).collect { (user, success) ->
                if (success) {
                    withContext(CommonDispatcher.Main) {
                        onLogin()
                    }
                } else {
                    isErroneous.emit(true)
                }
            }
        }
    }

    @Composable
    override fun render() {
        LoginScreen(this)
    }
}