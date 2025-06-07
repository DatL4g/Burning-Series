package dev.datlag.mimasu.extension

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import dev.datlag.sekret.NativeLoader
import kotlinx.serialization.Serializable
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Keep
class AppInitializer : Initializer<AppInitializer.State> {

    @OptIn(ExperimentalAtomicApi::class)
    override fun create(context: Context): State {
        return State(
            sekretLoaded = NativeLoader.loadLibrary(context, SEKRET_LIB)
        ).also { result ->
            state.store(result)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }

    @Serializable
    data class State(
        val sekretLoaded: Boolean
    )

    companion object {
        @OptIn(ExperimentalAtomicApi::class)
        private val state = AtomicReference<State?>(null)
        private const val SEKRET_LIB = "sekret"

        @OptIn(ExperimentalAtomicApi::class)
        fun isSekretLoaded(context: Context): Boolean {
            return state.load()?.sekretLoaded?.takeIf { it } ?: androidx.startup.AppInitializer
                .getInstance(context)
                .initializeComponent(AppInitializer::class.java)
                .sekretLoaded.takeIf { it }
            ?: NativeLoader.loadLibrary(context, SEKRET_LIB)
        }
    }
}