package de.datlag.burningseries.ui.fragment

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.DeviceDiscoveryAdapter
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentSyncBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.model.*
import de.datlag.burningseries.viewmodel.SyncViewModel
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@Obfuscate
class SyncFragment : AdvancedFragment(R.layout.fragment_sync) {

    private val binding: FragmentSyncBinding by viewBinding(FragmentSyncBinding::bind)
    private val syncViewModel: SyncViewModel by viewModels()
    private val discoveryAdapter = DeviceDiscoveryAdapter()

    @Inject
    lateinit var json: Json

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        syncViewModel.makeDiscoverable(getDeviceName(), HostOptionalInfo(isTelevision))
        syncViewModel.startDiscovery()
        syncViewModel.startReceiving()

        discoveryAdapter.setOnClickListener {
            syncViewModel.request(it)
        }
        discoveryRecycler.adapter = discoveryAdapter

        collectPeers()
        collectConnection()
    }

    private fun collectPeers() = syncViewModel.peers.launchAndCollect {
        discoveryAdapter.submitList(it)
    }

    private fun collectConnection() = syncViewModel.receiveData.launchAndCollect { (host, received) ->
        when (val syncModel = json.decodeFromString<SyncModel>(String(received))) {
            is SyncRequest -> {
                // do following methods only if user accepted in dialog
                binding.discoveryRecycler.hide()
                binding.syncProgress.show()
                syncViewModel.onRequest(host, syncModel.amount, binding.syncProgress.getProgress()) { progress ->
                    if (progress >= 1F) {
                        syncFinished()
                    }
                    binding.syncProgress.setProgress(progress)
                }
            }
            is SyncRequestAccept -> {
                binding.discoveryRecycler.hide()
                binding.syncProgress.show()
                syncViewModel.onAccepted(host, syncModel.amount, binding.syncProgress.getProgress()) { progress ->
                    if (progress >= 1F) {
                        syncFinished()
                    }
                    binding.syncProgress.setProgress(progress)
                }
            }
            is Sync -> {
                syncViewModel.onSync(syncModel.data, binding.syncProgress.getProgress()) { progress ->
                    if (progress >= 1F) {
                        syncFinished()
                    }
                    binding.syncProgress.setProgress(progress)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
    }

    private fun syncFinished() {
        Timber.e("Sync finished")
    }

    private fun getDeviceName(): String {
        val userName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                Settings.Global.getString(safeContext.contentResolver, Settings.Global.DEVICE_NAME)?.trim()
            } catch (ignored: Exception) {
                null
            }
        } else {
            null
        }
        if (!userName.isNullOrEmpty()) {
            return userName
        }
        val bluetoothName = try {
            Settings.Secure.getString(safeContext.contentResolver, "bluetooth_name")?.trim()
        } catch (ignored: Exception) {
            null
        }
        if (!bluetoothName.isNullOrEmpty()) {
            return bluetoothName
        }
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
}