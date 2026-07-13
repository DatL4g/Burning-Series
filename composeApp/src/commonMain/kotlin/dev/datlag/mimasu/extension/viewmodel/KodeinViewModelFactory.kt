package dev.datlag.mimasu.extension.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.datlag.mimasu.extension.common.typeOf
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.compose.localDI
import org.kodein.di.instanceOrNull
import kotlin.reflect.KClass

class KodeinViewModelFactory(private val di: DirectDI) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return when {
            modelClass typeOf BurningSeriesViewModel::class -> {
                val firebaseWrapper = di.instanceOrNull<FirebaseWrapper.Creator>()?.let {
                    (it as? FirebaseWrapper.Creator.Available)?.wrapper
                } ?: di.instanceOrNull()
                val json = di.instanceOrNull<Json>() ?: Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }

                val model = BurningSeriesViewModel(
                    firebaseWrapper = firebaseWrapper,
                    json = json,
                    client = di.instanceOrNull<HttpClient>()
                )

                (model as? T) ?: super.create(modelClass, extras)
            }
            else -> super.create(modelClass, extras)
        }
    }
}

@Composable
inline fun <reified VM : ViewModel> kodeinViewModel(
    di: DI = localDI(),
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided vie LocalViewModelStoreOwner"
    },
    key: String? = null,
    extras: CreationExtras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
        viewModelStoreOwner.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    }
): VM {
    val factory by di.instanceOrNull<ViewModelProvider.Factory>()

    return viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        key = key,
        factory = factory,
        extras = extras
    )
}