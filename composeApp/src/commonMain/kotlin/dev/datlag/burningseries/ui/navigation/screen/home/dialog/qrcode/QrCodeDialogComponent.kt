package dev.datlag.burningseries.ui.navigation.screen.home.dialog.qrcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import dev.datlag.burningseries.k2k.connect.connection
import dev.datlag.burningseries.k2k.discover.discovery
import dev.datlag.burningseries.other.SyncHelper
import dev.datlag.nanoid.NanoIdUtils
import dev.datlag.tooling.Platform
import dev.datlag.tooling.decompose.ioScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance

class QrCodeDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onDismiss: () -> Unit
) : QrCodeComponent, ComponentContext by componentContext {

    private val syncHelper by instance<SyncHelper>()
    override val syncedSettings: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val identifier: String = NanoIdUtils.randomNanoId()
    private val discovery = ioScope().discovery {
        setPort(1337)
        setDiscoverableTimeout(0L)
        setDiscoveryTimeout(1L) // builder requires value, stop immediately if used for discovery
    }

    private val connection = ioScope().connection {
        setPort(1338)
    }
    private val deviceName by instance<String>("DEVICE_NAME")

    init {
        discovery.makeDiscoverable(
            hostName = deviceName,
            filterMatch = identifier
        )

        connection.startReceiving { bytes ->
            val updated = syncHelper.updateSettingsFromByteArray(bytes)
            syncedSettings.update { updated }
        }
    }


    @Composable
    override fun render() {
        onRender {
            QrCodeDialog(this)
        }
    }

    override fun dismiss() {
        onDismiss()
    }

}

