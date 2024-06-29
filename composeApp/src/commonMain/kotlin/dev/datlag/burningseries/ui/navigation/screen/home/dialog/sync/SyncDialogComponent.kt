package dev.datlag.burningseries.ui.navigation.screen.home.dialog.sync

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import dev.datlag.burningseries.k2k.Host
import dev.datlag.burningseries.k2k.connect.Connection
import dev.datlag.burningseries.k2k.connect.connection
import dev.datlag.burningseries.k2k.discover.discovery
import dev.datlag.burningseries.other.SyncHelper
import dev.datlag.tooling.decompose.ioScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import org.kodein.di.DI
import org.kodein.di.instance

class SyncDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val connectId: String,
    private val onDismiss: () -> Unit
) : SyncComponent, ComponentContext by componentContext {

    private val syncHelper by instance<SyncHelper>()
    override val deviceNotFound: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val sending: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val discovery = ioScope().discovery {
        setPort(1337)
        setDiscoveryTimeout(5000L)
        setDiscoveryTimeoutListener {
            deviceNotFound.update { true }
        }
        setDiscoverableTimeout(1L)
        setHostIsClient(false)
        setHostFilter("^$connectId$".toRegex())
    }
    private val connect = ioScope().connection {
        setPort(1338)
    }

    init {
        discovery.startDiscovery()

        launchIO {
            val matchingPeer = discovery.peers.firstOrNull { it.size >= 1 }?.firstOrNull()

            matchingPeer?.let {
                connect(it)
            } ?: deviceNotFound.update { true }
        }
    }

    @Composable
    override fun render() {
        onRender {
            SyncDialog(this)
        }
    }

    override fun dismiss() {
        onDismiss()
    }

    private suspend fun connect(host: Host) {
        discovery.stopDiscovery()

        deviceNotFound.update { false }
        sending.update { true }
        while (currentCoroutineContext().isActive) {
            connect.send(syncHelper.encodeSettingsToByteArray(), host)
            delay(3000)
        }
        sending.update { false }
    }
}