package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dolatkia.animatedThemeManager.AppTheme
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.LatestEpisodeRecyclerAdapter
import de.datlag.burningseries.adapter.LatestSeriesRecyclerAdapter
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentHomeBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.burningseries.viewmodel.GitHubViewModel
import de.datlag.burningseries.viewmodel.SettingsViewModel
import de.datlag.burningseries.viewmodel.UserViewModel
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@AndroidEntryPoint
@Obfuscate
class HomeFragment : AdvancedFragment(R.layout.fragment_home) {

	private val binding: FragmentHomeBinding by viewBinding()
	private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()
	private val settingsViewModel: SettingsViewModel by activityViewModels()
	private val gitHubViewModel: GitHubViewModel by activityViewModels()
	private val userViewModel: UserViewModel by activityViewModels()

	private val latestEpisodeRecyclerAdapter by lazy {
		LatestEpisodeRecyclerAdapter(binding.allSeriesButton.id)
	}
	private val latestSeriesRecyclerAdapter by lazy {
		LatestSeriesRecyclerAdapter(binding.allSeriesButton.id, extendedFab?.id)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		hideKeyboard()
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.settingsBadge.translationZ = Float.MAX_VALUE

		initRecycler()
		checkIfErrorCaught()
		listenImproveDialogSetting()
		listenNewVersionDialog()
		listenAllSeriesCount()
		recoverMalAuthState()
		recoverAniListAuthState()

		burningSeriesViewModel.homeData.distinctUntilChanged().launchAndCollect {
			when (it.status) {
				Resource.Status.LOADING -> {
					showLoadingStatusBar()
				}
				Resource.Status.SUCCESS -> {
					latestSeriesRecyclerAdapter.submitList(it.data?.latestSeries ?: listOf())
					latestEpisodeRecyclerAdapter.submitList(it.data?.latestEpisodes ?: listOf())
					showSuccessStatusBar()
				}
				Resource.Status.ERROR -> {
					showErrorStatusBar()
				}
			}
		}

		loadImageAndSave(Constants.BS_TO_HEADER) {
			binding.banner.load<Drawable>(it)
		}

		binding.allSeriesButton.setOnClickListener {
			findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToAllSeriesFragment())
		}

		binding.settingsButton.setOnClickListener {
			findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToSettingsFragment())
		}

		extendedFabFavorite(HomeFragmentDirections.actionHomeFragmentToFavoritesFragment())
		extendedFab?.id?.let { binding.allSeriesButton.nextFocusRightId = it }
	}

	override fun syncTheme(appTheme: AppTheme) {
		val currentTheme = appTheme as? ApplicationTheme?
		currentTheme?.let {
			binding.parent.setBackgroundColor(it.defaultBackgroundColor(safeContext))
			binding.episodesHeader.setTextColor(it.defaultContentColor(safeContext))
			binding.settingsButton.setBackgroundColor(it.defaultContentColor(safeContext))
			binding.settingsButton.setTextColor(it.defaultBackgroundColor(safeContext))
			binding.seriesHeader.setTextColor(it.defaultContentColor(safeContext))
			binding.allSeriesButton.setBackgroundColor(it.defaultContentColor(safeContext))
			binding.allSeriesButton.setTextColor(it.defaultBackgroundColor(safeContext))

			latestEpisodeRecyclerAdapter.resubmitList()
		}
	}

	private fun showSettingsBadgeWith(text: CharSequence?, success: Boolean) {
		binding.settingsBadge.setText(text, false)
		binding.settingsBadge.badgeBackgroundDrawable = if (success) {
			AppCompatResources.getDrawable(safeContext, R.drawable.badge_green)
		} else {
			AppCompatResources.getDrawable(safeContext, R.drawable.badge_red)
		}
		binding.settingsBadge.invisible()

		binding.settingsBadge.post {
			val params = binding.settingsBadge.layoutParams as ViewGroup.MarginLayoutParams
			params.apply {
				var measuredW = binding.settingsBadge.measuredWidth
				if (measuredW == 0) {
					measuredW = binding.settingsBadge.width
				}

				var measuredH = binding.settingsBadge.measuredHeight
				if (measuredH == 0) {
					measuredH = binding.settingsBadge.height
				}
				leftMargin = -(measuredW / 2)
				topMargin = -(measuredH / 4)
			}
			binding.settingsBadge.layoutParams = params
			binding.settingsBadge.show()
		}
	}

	private fun checkIfErrorCaught() {
		val errorText = loadFileSavedText(Constants.LOG_FILE)?.trim()
		if (!errorText.isNullOrEmpty()) {
			showSettingsBadgeWith(safeContext.getString(R.string.exclamation_mark), false)
		}
	}

	private fun listenImproveDialogSetting() = settingsViewModel.data.map { it.appearance.improveDialog }.launchAndCollect {
		if (it) {
			if (!burningSeriesViewModel.showedHelpImprove) {
				getBurningSeriesHosterCount().launchAndCollect { count ->
					while (view != null && !burningSeriesViewModel.showedHelpImprove) {
						try {
							findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHelpImproveDialog(count))
							burningSeriesViewModel.showedHelpImprove = true
							break
						} catch (ignored: Exception) { }
						withContext(Dispatchers.IO) {
							delay(1000)
						}
					}
				}
			}
		} else {
			burningSeriesViewModel.showedHelpImprove = true
		}
	}

	private fun recoverMalAuthState() = settingsViewModel.data.map { it.user.malAuth }.launchAndCollect {
		userViewModel.loadMalAuth(it)
	}

	private fun recoverAniListAuthState() = settingsViewModel.data.map { it.user.anilistAuth }.launchAndCollect {
		userViewModel.loadAniListAuth(it)
	}

	private fun listenNewVersionDialog() = gitHubViewModel.getLatestRelease().launchAndCollect {
		if (view != null && it != null) {
			showSettingsBadgeWith(safeContext.getString(R.string.arrow_up), true)
		}
		while (view != null && !gitHubViewModel.showedNewVersion && it != null) {
			if (burningSeriesViewModel.showedHelpImprove) {
				try {
					findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNewReleaseDialog(it))
					gitHubViewModel.showedNewVersion = true
					break
				} catch (ignored: Exception) { }
			}
			withContext(Dispatchers.IO) {
				delay(1000)
			}
		}
	}

	private fun listenAllSeriesCount() = burningSeriesViewModel.getAllSeriesCountJoined().distinctUntilChanged().launchAndCollect {
		if (it <= 0) {
			burningSeriesViewModel.allSeriesPagination.launchAndCollect {
				burningSeriesViewModel.getNewPaginationData()
			}
		}
	}

	private fun initRecycler(): Unit = with(binding) {
		latestEpisodeRecycler.adapter = latestEpisodeRecyclerAdapter
		latestEpisodeRecyclerAdapter.setOnClickListener { item ->
			findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToSeriesFragment(
				latestEpisode = item
			))
		}
		
		latestEpisodeRecyclerAdapter.setOnLongClickListener { item ->
			val (title, episode) = item.getEpisodeAndSeries()
			findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToOpenInBrowserDialog(
				Constants.getBurningSeriesLink(item.href),
				"$episode ${safeContext.getString(R.string.of)} \"$title"
			))
			true
		}
		

		latestSeriesRecycler.adapter = latestSeriesRecyclerAdapter
		latestSeriesRecyclerAdapter.setOnClickListener { item ->
			findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToSeriesFragment(
				latestSeries = item
			))
		}

		latestSeriesRecyclerAdapter.setOnLongClickListener { item ->
			findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToOpenInBrowserDialog(
				Constants.getBurningSeriesLink(item.href),
				item.title
			))
			true
		}
	}

	override fun onResume() {
		super.onResume()
		extendedFab?.visibility = View.VISIBLE
		hideNavigationFabs()
		burningSeriesViewModel.cancelFetchSeries()
		burningSeriesViewModel.setSeriesData(null)
	}
}