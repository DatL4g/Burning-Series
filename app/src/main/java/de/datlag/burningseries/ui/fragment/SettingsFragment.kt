package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
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
import de.datlag.coilifier.ImageLoader
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
@Obfuscate
class SettingsFragment : AdvancedFragment(R.layout.fragment_settings) {

    private val binding: FragmentSettingsBinding by viewBinding(FragmentSettingsBinding::bind)
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val gitHubViewModel: GitHubViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private val settingsAdapter = SettingsRecyclerAdapter()

    private val malOAuthResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result -> userViewModel.malResultLauncherCallback(result)
    }

    private val anilistOAuthResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result -> userViewModel.aniListResultLauncherCallback(result)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()
        setSettingsData()
        listenNewVersion()

        userViewModel.setSaveMalAuthListener {
            settingsViewModel.updateUserMalAuth(it)
        }
        userViewModel.setSaveAniListListener {
            settingsViewModel.updateUserAniListAuth(it)
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
    }

    private fun initRecycler(): Unit = with(binding) {
        settingsRecycler.isNestedScrollingEnabled = false
        settingsRecycler.adapter = settingsAdapter
    }

    private fun setSettingsData() = settingsViewModel.data.launchAndCollect {
        userViewModel.loadMalAuth(it.user.malAuth)
        userViewModel.loadAniListAuth(it.user.anilistAuth)

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
            SettingsModel.Group(2, getString(R.string.myanimelist)),
            SettingsModel.Service(0,
                getString(R.string.mal_login),
                getString(R.string.mal_login_subtitle),
                getString(if (userViewModel.isMalAuthorized()) R.string.logout else R.string.login),
                { view ->
                    userViewModel.getUserMal { mal ->
                        loadUserImage(mal, view)
                    }
                }
            ) {
                if (userViewModel.isMalAuthorized()) {
                    userViewModel.endMalAuth()
                } else {
                    malOAuthResultLauncher.launch(userViewModel.createMalAuthIntent(safeContext))
                }
            },
            SettingsModel.Switch(6,
                getString(R.string.mal_images),
                getString(R.string.mal_images_subtitle),
                it.user.malImages && userViewModel.isMalAuthorized(),
                userViewModel.isMalAuthorized()
            ) { isChecked ->
                settingsViewModel.updateUserMalImages(isChecked)
            },
            SettingsModel.Group(3, getString(R.string.anilist)),
            SettingsModel.Service(1,
                getString(R.string.anilist_login),
                getString(R.string.anilist_login_subtitle),
                getString(if (userViewModel.isAniListAuthorized()) R.string.logout else R.string.login),
                { view ->
                    loadAniListUserImage(view)
                }
            ) {
                if (userViewModel.isAniListAuthorized()) {
                    userViewModel.endAniListAuth()
                } else {
                    anilistOAuthResultLauncher.launch(userViewModel.createAniListAuthIntent(safeContext))
                }
            },
            SettingsModel.Switch(7,
                getString(R.string.anilist_images),
                getString(R.string.anilist_images_subtitle),
                it.user.aniListImages && userViewModel.isAniListAuthorized(),
                userViewModel.isAniListAuthorized()
            ) { isChecked ->
                settingsViewModel.updateUserAniListImages(isChecked)
            },
        ))
    }

    private fun loadUserImage(mal: MyAnimeList?, view: ImageView) = lifecycleScope.launch(Dispatchers.IO) {
        val picture = mal?.authenticatedUser?.pictureURL
        withContext(Dispatchers.Main) {
            view.clearTint()
            if (picture.isNullOrEmpty()) {
                view.load<Drawable>(R.drawable.ic_myanimelist)
            } else {
                view.load<Drawable>(picture) {
                    transform(RoundedCorners(safeContext.dpToPx(12).toInt()))
                    error(R.drawable.ic_myanimelist)
                    placeholder(R.drawable.ic_myanimelist)
                }
            }
        }
    }

    private fun loadAniListUserImage(view: ImageView) = userViewModel.getAniListUser().launchAndCollect {
        val picture = it?.avatar?.large ?: it?.avatar?.medium
        withContext(Dispatchers.Main) {
            view.clearTint()
            if (picture.isNullOrEmpty()) {
                view.load<Drawable>(R.drawable.ic_anilist)
            } else {
                view.load<Drawable>(picture) {
                    transform(RoundedCorners(safeContext.dpToPx(12).toInt()))
                    error(R.drawable.ic_anilist)
                    placeholder(R.drawable.ic_anilist)
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