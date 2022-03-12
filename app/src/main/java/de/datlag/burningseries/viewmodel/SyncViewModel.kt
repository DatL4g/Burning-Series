package de.datlag.burningseries.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.burningseries.model.*
import de.datlag.k2k.Host
import de.datlag.k2k.connect.Connection
import de.datlag.k2k.connect.connection
import de.datlag.k2k.discover.Discovery
import de.datlag.k2k.discover.discovery
import de.datlag.model.burningseries.series.relation.SeriesWithEpisode
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.network.burningseries.BurningSeriesRepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
@Obfuscate
class SyncViewModel @Inject constructor(
    val repository: BurningSeriesRepository,
    val json: Json,
    @Named("packageName") val packageName: String
) : ViewModel() {

    private val seriesToSync: MutableList<SeriesWithInfo> = mutableListOf()

    private val discovery: Discovery = viewModelScope.discovery {
        setPuffer(3000)
        setDiscoveryTimeout(0)
        setDiscoverableTimeout(0)
        setPort(6969)
        setHostFilter(Regex(packageName))
    }
    private val connection: Connection = viewModelScope.connection {
        setPort(7070)
        fromDiscovery(discovery)
    }

    private var otherSyncAmount: Int = 0
    private var otherSynced: Int = 0

    val peers: Flow<Set<Host>> = discovery.peersFlow

    val receiveData: Flow<Pair<Host, ByteArray>> = connection.receiveData

    fun makeDiscoverable(name: String, hostOptional: HostOptionalInfo) = discovery.makeDiscoverable(
        name,
        packageName,
        json.encodeToJsonElement(hostOptional)
    )

    fun startDiscovery() = discovery.startDiscovery()

    fun startReceiving() = connection.startReceiving()

    fun stopBeingDiscoverable() = discovery.stopBeingDiscoverable()

    fun stopDiscovery() = discovery.stopDiscovery()

    fun stopReceiving() = connection.stopReceiving()

    fun getSeriesToSync() = repository.getSeriesToSync()

    fun saveSyncSeries(seriesWithEpisode: SeriesWithInfo) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveSyncSeries(seriesWithEpisode)
    }

    fun request(host: Host) = viewModelScope.launch(Dispatchers.IO) {
        seriesToSync.clear()
        seriesToSync.addAll(getSeriesToSync().first())
        Timber.e("SeriesToSync: $seriesToSync")
        val data: SyncModel = SyncRequest(seriesToSync.size)
        connection.send(json.encodeToString(data).toByteArray(), host)
    }

    suspend fun onRequest(host: Host, amount: Int, viewProgress: Float, setProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        otherSyncAmount = amount
        otherSynced = 0
        if (seriesToSync.isEmpty()) {
            seriesToSync.clear()
            seriesToSync.addAll(getSeriesToSync().first())
        }
        if (seriesToSync.isEmpty()) {
            withContext(Dispatchers.Main) {
                if (viewProgress < 0.5) {
                    setProgress(0.5F)
                } else if (viewProgress > 0.5) {
                    setProgress(1F)
                }
            }
        } else {
            val steps = (50F / seriesToSync.size.toFloat()) / 100F
            val data: SyncModel = SyncRequestAccept(seriesToSync.size)
            connection.send(json.encodeToString(data).toByteArray(), host)
            sendAndSetProgress(host, viewProgress, steps, setProgress)
        }
    }

    suspend fun onAccepted(host: Host, amount: Int, viewProgress: Float, setProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        otherSyncAmount = amount
        otherSynced = 0
        val steps = (50F / seriesToSync.size.toFloat()) / 100F
        sendAndSetProgress(host, viewProgress, steps, setProgress)
    }

    suspend fun onSync(data: SeriesWithInfo, viewProgress: Float, setProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        saveSyncSeries(data)
        otherSynced++
        val steps = (50F / otherSyncAmount.toFloat()) / 100F
        var progress: Float = viewProgress
        if (otherSynced >= otherSyncAmount) {
            if (progress < 0.5 && progress > 0F) {
                progress = 0.5F
            } else if (progress > 0.5) {
                progress = 1F
            }
        } else {
            progress += steps
        }
        withContext(Dispatchers.Main) {
            setProgress(progress)
        }
    }

    private suspend fun sendAndSetProgress(host: Host, viewProgress: Float, steps: Float, setProgress: (Float) -> Unit) {
        var progress = viewProgress
        seriesToSync.forEach { series ->
            val data: SyncModel = Sync(series)
            connection.send(json.encodeToString(data).toByteArray(), host)
            progress += steps
            withContext(Dispatchers.Main) {
                setProgress(progress)
            }
            delay(1000)
        }
        if (progress < 0.5 && progress > 0F) {
            progress = 0.5F
        } else if (progress > 0.5) {
            progress = 1F
        }
        withContext(Dispatchers.Main) {
            setProgress(progress)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopBeingDiscoverable()
        stopDiscovery()
        stopReceiving()
        try {
            viewModelScope.cancel()
        } catch (ignored: Exception) { }
    }
}