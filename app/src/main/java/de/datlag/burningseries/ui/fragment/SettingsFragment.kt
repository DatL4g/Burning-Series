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
import com.dolatkia.animatedThemeManager.AppTheme
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
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.burningseries.ui.theme.BurningSeriesTheme
import de.datlag.burningseries.ui.theme.DefaultTheme
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
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
            findNavController().safeNavigate(SettingsFragmentDirections.actionSettingsFragmentToAboutLibraries())
        }
        binding.githubCard.setOnClickListener {
            Constants.GITHUB_PROJECT.toUri().openInBrowser(safeContext, safeContext.getString(R.string.github_project))
        }
        binding.syncCard.setOnClickListener {
            findNavController().safeNavigate(SettingsFragmentDirections.actionSettingsFragmentToSyncFragment())
        }

        val errorText = loadFileSavedText(Constants.LOG_FILE)?.trim()
        if (errorText.isNullOrEmpty()) {
            binding.latestErrorCard.hide()
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
                moreLabelColor(safeContext.getColorCompat(R.color.datlagContentColor))
                lessLabelColor(safeContext.getColorCompat(R.color.datlagContentColor))
                expandAnimation(true)
            }
            try {
                readMoreOption.addReadMoreTo(binding.errorText, errorText)
            } catch (ignored: Exception) {
                binding.errorText.text = errorText
            }
            binding.errorClearButton.setOnClickListener {
                clearTextFile(Constants.LOG_FILE)
                binding.latestErrorCard.hide()
            }
            binding.errorCopyButton.setOnClickListener {
                safeContext.copyToClipboard(safeContext.getString(R.string.error_copy_tag), errorText)
            }
            binding.latestErrorCard.show()
        }
    }

    override fun syncTheme(appTheme: AppTheme) {
        val currentTheme = appTheme as? ApplicationTheme?
        currentTheme?.let {
            binding.parent.setBackgroundColor(it.defaultBackgroundColor(safeContext))
            binding.settingsHeader.setTextColor(it.defaultContentColor(safeContext))
            binding.latestReleaseCard.setCardBackgroundColor(it.defaultContentColor(safeContext))
            binding.title.setTextColor(it.defaultBackgroundColor(safeContext))
            binding.date.setTextColor(it.defaultBackgroundColor(safeContext))
            binding.text.setTextColor(it.defaultBackgroundColor(safeContext))
            binding.viewButton.setTextColor(it.defaultContentColor(safeContext))
            binding.viewButton.setBackgroundColor(it.defaultBackgroundColor(safeContext))

            binding.librariesCard.setCardBackgroundColor(it.defaultContentColor(safeContext))
            binding.aboutIcon.clearTint()
            binding.aboutIcon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(it.defaultBackgroundColor(safeContext), BlendModeCompat.SRC_IN)
            binding.about.setTextColor(it.defaultBackgroundColor(safeContext))

            binding.githubCard.setCardBackgroundColor(it.defaultContentColor(safeContext))
            binding.githubIcon.clearTint()
            binding.githubIcon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(it.defaultBackgroundColor(safeContext), BlendModeCompat.SRC_IN)
            binding.github.setTextColor(it.defaultBackgroundColor(safeContext))

            binding.syncCard.setCardBackgroundColor(it.defaultContentColor(safeContext))
            binding.syncIcon.clearTint()
            binding.syncIcon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(it.defaultBackgroundColor(safeContext), BlendModeCompat.SRC_IN)
            binding.sync.setTextColor(it.defaultBackgroundColor(safeContext))
        }
    }

    private fun initRecycler(): Unit = with(binding) {
        settingsRecycler.isNestedScrollingEnabled = false
        settingsRecycler.adapter = settingsAdapter
    }

    private fun setSettingsData() = settingsViewModel.data.distinctUntilChanged().launchAndCollect {
        userViewModel.loadMalAuth(it.user.malAuth)
        userViewModel.loadAniListAuth(it.user.anilistAuth)

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
                safeContext.getString(R.string.burningseries_theme),
                safeContext.getString(R.string.burningseries_theme_subtitle),
                it.appearance.theme > 0
            ) { view, isChecked ->
                val id = if (isChecked) {
                    themeManager.changeTheme(BurningSeriesTheme(), view, 0)
                    1
                } else {
                    themeManager.reverseChangeTheme(DefaultTheme(), view, 0)
                    0
                }
                settingsAdapter.notifyDataSetChanged()

                settingsViewModel.updateAppearanceBurningSeriesTheme(id)
            },
            SettingsModel.Switch(2,
                safeContext.getString(R.string.display_improve_dialog),
                safeContext.getString(R.string.display_improve_dialog_subtitle),
                it.appearance.improveDialog
            ) { _, isChecked ->
                settingsViewModel.updateAppearanceImproveDialog(isChecked)
            },
            SettingsModel.Group(1, safeContext.getString(R.string.video)),
            SettingsModel.Switch(3,
                safeContext.getString(R.string.advanced_fetching),
                safeContext.getString(R.string.advanced_fetching_subtitle),
                it.video.advancedFetching
            ) { _, isChecked ->
                settingsViewModel.updateVideoAdvancedFetching(isChecked)
            },
            SettingsModel.Switch(4,
                safeContext.getString(R.string.prefer_mp4),
                safeContext.getString(R.string.prefer_mp4_subtitle),
                it.video.preferMp4
            ) { _, isChecked ->
                settingsViewModel.updateVideoPreferMp4(isChecked)
            },
            SettingsModel.Switch(5,
                safeContext.getString(R.string.enable_preview),
                safeContext.getString(R.string.enable_preview_subtitle),
                it.video.previewEnabled
            ) { _, isChecked ->
                settingsViewModel.updateVideoPreview(isChecked)
            },
            SettingsModel.Switch(6,
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
            SettingsModel.Switch(7,
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
            SettingsModel.Switch(8,
                getString(R.string.anilist_images),
                getString(R.string.anilist_images_subtitle),
                it.user.aniListImages && userViewModel.isAniListAuthorized(),
                userViewModel.isAniListAuthorized()
            ) { _, isChecked ->
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
                    fallback(R.drawable.ic_myanimelist)
                }
            }
        }
    }

    private fun loadAniListUserImage(view: ImageView) = userViewModel.getAniListUser().distinctUntilChanged().launchAndCollect {
        val picture = it?.avatar?.large ?: it?.avatar?.medium
        withContext(Dispatchers.Main) {
            if (picture.isNullOrEmpty()) {
                view.load<Drawable>(R.drawable.ic_anilist)
            } else {
                view.load<Drawable>(picture) {
                    transform(RoundedCorners(safeContext.dpToPx(12).toInt()))
                    error(R.drawable.ic_anilist)
                    placeholder(R.drawable.ic_anilist)
                    fallback(R.drawable.ic_anilist)
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
                if (safeContext.isInstalledFromFDroid()) {
                    "${Constants.F_DROID_PACKAGES_URL}/${safeContext.packageName}".toUri()
                } else {
                    release.htmlUrl.toUri()
                }.openInBrowser(safeContext)
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