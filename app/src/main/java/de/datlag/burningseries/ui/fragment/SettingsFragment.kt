package de.datlag.burningseries.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.SettingsRecyclerAdapter
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.FragmentSettingsBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.model.SettingsModel
import de.datlag.burningseries.viewmodel.SettingsViewModel
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
@Obfuscate
class SettingsFragment : AdvancedFragment(R.layout.fragment_settings) {

    val binding: FragmentSettingsBinding by viewBinding()
    val settingsViewModel: SettingsViewModel by viewModels()

    val settingsAdapter = SettingsRecyclerAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.e("Settings loaded")
        initRecycler()
        setSettingsData()
    }

    private fun initRecycler(): Unit = with(binding) {
        settingsRecycler.layoutManager = LinearLayoutManager(safeContext)
        settingsRecycler.isNestedScrollingEnabled = false
        settingsRecycler.adapter = settingsAdapter
    }

    private fun setSettingsData() = settingsViewModel.data.launchAndCollect {
        settingsAdapter.submitList(listOf(
            SettingsModel.Group("Video"),
            SettingsModel.Switch(
                "Advanced fetching",
                "More and maybe better results but takes longer",
                it.video.advancedFetching
            ) { isChecked ->
                Timber.e("Advanced fetching: $isChecked")
                settingsViewModel.updateVideoAdvancedFetching(isChecked)
            },
            SettingsModel.Switch(
                "Prefer MP4",
                "Usually m3u8 is used, could fix video issues on some devices",
                it.video.preferMp4
            ) { isChecked ->
                Timber.e("Prefer mp4: $isChecked")
                settingsViewModel.updateVideoPreferMp4(isChecked)
            }
        ))
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
    }
}