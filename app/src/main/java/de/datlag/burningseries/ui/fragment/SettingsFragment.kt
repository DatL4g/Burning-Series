package de.datlag.burningseries.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
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
import de.datlag.burningseries.helper.NightMode
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
            SettingsModel.Group(0,safeContext.getString(R.string.appearance)),
            SettingsModel.Switch(0,
                safeContext.getString(R.string.dark_mode),
                safeContext.getString(R.string.dark_mode_subtitle),
                it.appearance.darkMode
            ) { isChecked ->
                val mode = if (isChecked) NightMode.Mode.DARK else NightMode.Mode.LIGHT
                AppCompatDelegate.setDefaultNightMode(mode.toDelegateMode())
                settingsViewModel.updateAppearanceDarkMode(isChecked)
            },
            SettingsModel.Switch(1,
                safeContext.getString(R.string.display_improve_dialog),
                safeContext.getString(R.string.display_improve_dialog_subtitle),
                it.appearance.improveDialog
            ) { isChecked ->
                settingsViewModel.updateAppearanceImproveDialog(isChecked)
            },
            SettingsModel.Group(1, safeContext.getString(R.string.video)),
            SettingsModel.Switch(2,
                safeContext.getString(R.string.advanced_fetching),
                safeContext.getString(R.string.advanced_fetching_subtitle),
                it.video.advancedFetching
            ) { isChecked ->
                settingsViewModel.updateVideoAdvancedFetching(isChecked)
            },
            SettingsModel.Switch(3,
                safeContext.getString(R.string.prefer_mp4),
                safeContext.getString(R.string.prefer_mp4_subtitle),
                it.video.preferMp4
            ) { isChecked ->
                settingsViewModel.updateVideoPreferMp4(isChecked)
            },
            SettingsModel.Switch(4,
                safeContext.getString(R.string.enable_preview),
                safeContext.getString(R.string.enable_preview_subtitle),
                it.video.previewEnabled
            ) { isChecked ->
                settingsViewModel.updateVideoPreview(isChecked)
            }
        ))
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
    }
}