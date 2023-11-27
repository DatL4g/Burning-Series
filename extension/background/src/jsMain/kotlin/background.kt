import common.isNullOrEmpty
import de.jensklingenberg.ktorfit.ktorfitBuilder
import dev.datlag.burningseries.Sekret
import dev.datlag.burningseries.model.ExtensionMessage
import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.burningseries.model.common.scopeCatching
import dev.datlag.burningseries.model.state.SaveAction
import dev.datlag.burningseries.model.state.SaveState
import dev.datlag.burningseries.network.Firestore
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.realm.RealmLoader
import dev.datlag.burningseries.network.state.SaveStateMachine
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.promise
import kotlinx.serialization.json.Json

fun main() {
    val defaultJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    val client = HttpClient(Js) {
        developmentMode = false
        followRedirects = true

        install(ContentNegotiation) {
            json(defaultJson)
        }
    }
    val packageName = "dev.datlag.burningseries"
    val ktorfit = ktorfitBuilder {
        httpClient(client)
    }
    val jsonBaseKtor = ktorfit.build {
        baseUrl("https://jsonbase.com/")
    }
    val firebaseKtor = ktorfit.build {
        baseUrl("https://firestore.googleapis.com/v1/projects/${Sekret.firebaseProject(packageName)}/")
    }
    val app = Firebase.initialize(
        context = null,
        options = FirebaseOptions(
            applicationId = Sekret.firebaseApplication(packageName),
            apiKey = Sekret.firebaseApiKey(packageName),
            projectId = Sekret.firebaseProject(packageName)
        )
    )
    val store = Firebase.firestore(app)

    val jsonBase = jsonBaseKtor.create<JsonBase>()
    val firebase = firebaseKtor.create<Firestore>()

    val activator = SaveStateMachine(
        client = client,
        jsonBase = jsonBase,
        realmLoader = RealmLoader,
        firestore = store,
        firestoreApi = firebase
    )
    val activatorState = activator.state.flowOn(Dispatchers.Default).shareIn(GlobalScope, SharingStarted.Eagerly)

    val memorySaveState = mutableMapOf<String, Boolean>()

    browser.runtime.onMessage.addListener {
        val msg = if (it.message.isNullOrEmpty()) {
            it
        } else {
            it.message
        }?.toString()

        if (msg.isNullOrEmpty()) {
            return@addListener false
        }

        val message: ExtensionMessage? = scopeCatching {
            defaultJson.decodeFromString<ExtensionMessage>(msg!!)
        }.getOrNull() ?: scopeCatching {
            it.unsafeCast<ExtensionMessage?>()
        }.getOrNull()

        if (message.isNullOrEmpty()) {
            return@addListener false
        }

        return@addListener GlobalScope.promise {
            if (message!!.set && !message.url.isNullOrBlank()) {
                activator.dispatch(SaveAction.Save(
                    HosterScraping(
                        href = message.href,
                        url = message.url!!
                    ),
                    loadStream = false
                ))
                val result = activatorState.first { state ->
                    state is SaveState.Success || state is SaveState.Error
                }
                return@promise (result is SaveState.Success).also { saved ->
                    memorySaveState[message.href] = saved
                }
            } else {
                return@promise memorySaveState[message.href] ?: false
            }
        }
    }
}
