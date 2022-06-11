package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.devs.readmoreoption.ReadMoreOption
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
import de.datlag.model.common.asIsoString
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber

@AndroidEntryPoint
@Obfuscate
class SettingsFragment : AdvancedFragment(R.layout.fragment_settings) {

    private val binding: FragmentSettingsBinding by viewBinding()
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

    private val githubOAuthResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result -> userViewModel.githubResultLauncherCallback(result)
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
        userViewModel.setSaveGitHubAuthListener {
            settingsViewModel.updateUserGitHubAuth(it)
        }
        binding.version.text = safeContext.getString(R.string.copyright,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year,
            Constants.GITHUB_OWNER
        )

        val errorText = loadFileSavedText(Constants.LOG_FILE)?.trim()
        if (errorText.isNullOrEmpty()) {
            binding.latestErrorCard.gone()
        } else {
            val errorLastModified = fileLastModifiedOrCreated(Constants.LOG_FILE)
            if (errorLastModified > 0L) {
                val localDate = Instant.fromEpochMilliseconds(errorLastModified).toLocalDateTime(TimeZone.currentSystemDefault()).date
                binding.errorDate.text = localDate.asIsoString()
            }
            val readMoreOption = safeContext.readMoreOption {
                textLength(3)
                textLengthType(ReadMoreOption.TYPE_LINE)
                moreLabel("\t${safeContext.getString(R.string.more)}")
                lessLabel("\t${safeContext.getString(R.string.less)}")
                labelUnderLine(true)
                expandAnimation(true)
            }
            try {
                readMoreOption.addReadMoreTo(binding.errorText, errorText)
            } catch (ignored: Exception) {
                binding.errorText.text = errorText
            }
            binding.errorClearButton.setOnClickListener {
                clearTextFile(Constants.LOG_FILE)
                binding.latestErrorCard.gone()
            }
            binding.errorCopyButton.setOnClickListener {
                safeContext.copyToClipboard(safeContext.getString(R.string.error_copy_tag), errorText)
            }
            binding.latestErrorCard.visible()
        }
    }

    private fun initRecycler(): Unit = with(binding) {
        settingsRecycler.isNestedScrollingEnabled = false
        settingsRecycler.adapter = settingsAdapter
    }

    private fun setSettingsData() = settingsViewModel.data.distinctUntilChanged().launchAndCollect {
        userViewModel.loadMalAuth(it.user.malAuth)
        userViewModel.loadAniListAuth(it.user.anilistAuth)
        userViewModel.loadGitHubAuth(it.user.githubAuth)

        settingsAdapter.submitList(listOf(
            SettingsModel.Group(0,safeContext.getString(R.string.appearance)),
            SettingsModel.Switch(0,
                safeContext.getString(R.string.dark_mode),
                safeContext.getString(R.string.dark_mode_subtitle),
                it.appearance.darkMode
            ) { _, isChecked ->
                val mode = if (isChecked) NightMode.Mode.DARK else NightMode.Mode.LIGHT

                AppCompatDelegate.setDefaultNightMode(mode.toDelegateMode())

                settingsViewModel.updateAppearanceDarkMode(isChecked)
            },
            SettingsModel.Switch(1,
                safeContext.getString(R.string.display_improve_dialog),
                safeContext.getString(R.string.display_improve_dialog_subtitle),
                it.appearance.improveDialog
            ) { _, isChecked ->
                settingsViewModel.updateAppearanceImproveDialog(isChecked)
            },
            SettingsModel.Group(1, safeContext.getString(R.string.video)),
            SettingsModel.Switch(2,
                safeContext.getString(R.string.advanced_fetching),
                safeContext.getString(R.string.advanced_fetching_subtitle),
                it.video.advancedFetching
            ) { _, isChecked ->
                settingsViewModel.updateVideoAdvancedFetching(isChecked)
            },
            SettingsModel.Switch(3,
                safeContext.getString(R.string.prefer_mp4),
                safeContext.getString(R.string.prefer_mp4_subtitle),
                it.video.preferMp4
            ) { _, isChecked ->
                settingsViewModel.updateVideoPreferMp4(isChecked)
            },
            SettingsModel.Switch(4,
                safeContext.getString(R.string.enable_preview),
                safeContext.getString(R.string.enable_preview_subtitle),
                it.video.previewEnabled
            ) { _, isChecked ->
                settingsViewModel.updateVideoPreview(isChecked)
            },
            SettingsModel.Switch(5,
                getString(R.string.default_fullscreen),
                getString(R.string.default_fullscreen_subtitle),
                it.video.defaultFullscreen
            ) { _, isChecked ->
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
            ) { _, isChecked ->
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
            ) { _, isChecked ->
                settingsViewModel.updateUserAniListImages(isChecked)
            },
            SettingsModel.Group(4, getString(R.string.github)),
            SettingsModel.Service(2,
                getString(R.string.github_login),
                getString(R.string.github_login_subtitle),
                getString(if (userViewModel.isGitHubAuthorized()) R.string.logout else R.string.login),
                { view ->
                    loadGitHubUser(view)
                }
            ) {
                if (userViewModel.isGitHubAuthorized()) {
                    userViewModel.endGitHubAuth()
                } else {
                    githubOAuthResultLauncher.launch(userViewModel.createGitHubAuthIntent(safeContext))
                }
            }
        ))
    }

    private fun loadUserImage(mal: MyAnimeList?, view: ImageView) = lifecycleScope.launch(Dispatchers.IO) {
        val authUser = mal?.authenticatedUser
        val picture = authUser?.pictureURL
        val name = authUser?.name
        withContext(Dispatchers.Main) {
            view.load<Drawable>(picture) {
                transform(RoundedCorners(safeContext.dpToPx(12).toInt()))
                error(R.drawable.ic_myanimelist)
                placeholder(R.drawable.ic_myanimelist)
                fallback(R.drawable.ic_myanimelist)
            }
            view.contentDescription = name
        }
    }

    private fun loadAniListUserImage(view: ImageView) = userViewModel.getAniListUser().distinctUntilChanged().launchAndCollect {
        view.load<Drawable>(it?.avatar?.large ?: it?.avatar?.medium) {
            transform(RoundedCorners(safeContext.dpToPx(12).toInt()))
            error(R.drawable.ic_anilist)
            placeholder(R.drawable.ic_anilist)
            fallback(R.drawable.ic_anilist)
        }
        view.contentDescription = it?.name
    }

    private fun loadGitHubUser(view: ImageView) = userViewModel.getGitHubUser().distinctUntilChanged().launchAndCollect {
        view.load<Drawable>(it?.avatarUrl) {
            transform(RoundedCorners(safeContext.dpToPx(12).toInt()))
            error(R.drawable.ic_github)
            placeholder(R.drawable.ic_github)
            fallback(R.drawable.ic_github)
        }
        view.contentDescription = it?.name ?: it?.login
    }

    private fun listenNewVersion() = gitHubViewModel.getLatestRelease().launchAndCollect { release ->
        if (release != null) {
            binding.latestReleaseCard.visible()
            binding.date.text = release.publishedAtIsoDate()
            binding.text.text = safeContext.getString(
                R.string.new_release_text, release.tagName,
                BuildConfig.VERSION_NAME
            )
            binding.viewButton.setOnClickListener {
                if (safeContext.isInstalledFromFDroid()) {
                    "${Constants.F_DROID_PACKAGES_URL}/${safeContext.packageName}".toUri()
                } else {
                    release.htmlUrl.toUri()
                }.openInBrowser(safeContext)
            }
        } else {
            binding.latestReleaseCard.gone()
        }
    }

    override fun initActivityViews() {
        super.initActivityViews()

        exitFullScreen()
        hideSeriesArc()
        extendedFab?.gone()
        hideNavigationFabs()
        showToolbarBackButton()
        hideSeriesArc()
        appBarLayout?.setExpanded(false, false)
        appBarLayout?.setExpandable(false)
        setToolbarTitle(R.string.settings)
    }
}