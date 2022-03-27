package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.kttdevelopment.mal4j.MyAnimeList
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.BuildConfig
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.SettingsRecyclerAdapter
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentSettingsBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.helper.NightMode
import de.datlag.burningseries.model.SettingsModel
import de.datlag.burningseries.viewmodel.GitHubViewModel
import de.datlag.burningseries.viewmodel.SettingsViewModel
import de.datlag.burningseries.viewmodel.UserViewModel
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
@Obfuscate
class SettingsFragment : AdvancedFragment(R.layout.fragment_settings) {

    private val binding: FragmentSettingsBinding by viewBinding(FragmentSettingsBinding::bind)
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val gitHubViewModel: GitHubViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private val settingsAdapter = SettingsRecyclerAdapter()

    private val malOAuthResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result -> userViewModel.resultLauncherCallback(result)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()
        setSettingsData()
        listenNewVersion()
        userViewModel.setSaveMalAuthListener {
            settingsViewModel.updateUserMalAuth(it)
        }
        binding.librariesCard.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToAboutLibraries())
        }
        binding.githubCard.setOnClickListener {
            Constants.GITHUB_PROJECT.toUri().openInBrowser(safeContext, safeContext.getString(R.string.github_project))
        }
        binding.syncCard.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToSyncFragment())
        }
        binding.malCard.setOnClickListener {
            if (userViewModel.isMalAuthorized()) {
                userViewModel.endMalAuth()
            } else {
                malOAuthResultLauncher.launch(userViewModel.createMalAuthIntent(safeContext))
            }
        }
    }

    private fun initRecycler(): Unit = with(binding) {
        settingsRecycler.isNestedScrollingEnabled = false
        settingsRecycler.adapter = settingsAdapter
    }

    private fun setSettingsData() = settingsViewModel.data.launchAndCollect {
        userViewModel.loadMalAuth(it.user.malAuth)

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
            },
            SettingsModel.Switch(5,
                getString(R.string.default_fullscreen),
                getString(R.string.default_fullscreen_subtitle),
                it.video.defaultFullscreen
            ) { isChecked ->
                settingsViewModel.updateVideoFullscreen(isChecked)
            },
            SettingsModel.Group(2, "MyAnimeList"),
            SettingsModel.Switch(6,
                "Use MAL Images",
                "Loads MyAnimeList Covers of series",
                it.user.malImages && userViewModel.isMalAuthorized(),
                userViewModel.isMalAuthorized()
            ) { isChecked ->
                settingsViewModel.updateUserMalImages(isChecked)
            }
        ))
        binding.mal.text = safeContext.getString(if (userViewModel.isMalAuthorized()) R.string.mal_logout else R.string.mal_login)
        userViewModel.getUserMal { mal -> loadUserImage(mal) }
    }

    private fun loadUserImage(mal: MyAnimeList?) = lifecycleScope.launch(Dispatchers.IO) {
        val picture = mal?.authenticatedUser?.pictureURL
        withContext(Dispatchers.Main) {
            binding.malIcon.clearTint()
            if (picture.isNullOrEmpty()) {
                binding.malIcon.load<Drawable>(R.drawable.ic_myanimelist)
            } else {
                binding.malIcon.load<Drawable>(picture) {
                    transform(RoundedCorners(safeContext.dpToPx(12).toInt()))
                    error(R.drawable.ic_myanimelist)
                    placeholder(R.drawable.ic_myanimelist)
                }
            }
        }

    }

    private fun listenNewVersion() = gitHubViewModel.getLatestRelease().launchAndCollect { release ->
        if (release != null) {
            binding.latestReleaseCard.show()
            binding.date.text = release.publishedAtIsoDate()
            binding.text.text = safeContext.getString(
                R.string.new_release_text, release.tagName,
                BuildConfig.VERSION_NAME,
                safeContext.getString(if (release.isPreRelease) R.string.yes else R.string.no)
            )
            binding.viewButton.setOnClickListener {
                release.htmlUrl.toUri().openInBrowser(safeContext)
            }
        } else {
            binding.latestReleaseCard.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
    }
}