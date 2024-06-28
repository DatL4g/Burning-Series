package dev.datlag.burningseries.ui.navigation.screen.home.dialog.sync

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.k2k.Host
import dev.datlag.burningseries.k2k.connect.Connection
import dev.datlag.burningseries.k2k.connect.connection
import dev.datlag.burningseries.k2k.discover.discovery
import dev.datlag.tooling.decompose.ioScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

class SyncDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val connectId: String,
    private val onDismiss: () -> Unit
) : SyncComponent, ComponentContext by componentContext {

    override val deviceNotFound: MutableStateFlow<Boolean> = MutableStateFlow(false)
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

    init {
        discovery.startDiscovery()

        launchIO {
            val matchingPeer = discovery.peers.firstOrNull { it.size >= 1 }?.firstOrNull()

            matchingPeer?.let(::connect)
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

    private fun connect(host: Host) {
        discovery.stopDiscovery()

        val connect = ioScope().connection {
            setPort(1337)
            forPeer(host)
        }
    }
}