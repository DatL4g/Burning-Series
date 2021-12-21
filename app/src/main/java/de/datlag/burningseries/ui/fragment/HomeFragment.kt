package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.LatestEpisodeRecyclerAdapter
import de.datlag.burningseries.adapter.LatestSeriesRecyclerAdapter
import de.datlag.burningseries.common.hideLoadingDialog
import de.datlag.burningseries.common.openInBrowser
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.common.showLoadingDialog
import de.datlag.burningseries.databinding.FragmentHomeBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.burningseries.viewmodel.GitHubViewModel
import de.datlag.burningseries.viewmodel.SettingsViewModel
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
@Obfuscate
class HomeFragment : AdvancedFragment(R.layout.fragment_home) {
	
	private val binding: FragmentHomeBinding by viewBinding()
	private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()
	private val settingsViewModel: SettingsViewModel by viewModels()
	private val gitHubViewModel: GitHubViewModel by activityViewModels()
	
	private val latestEpisodeRecyclerAdapter by lazy {
		LatestEpisodeRecyclerAdapter(binding.allSeriesButton.id)
	}
	private val latestSeriesRecyclerAdapter by lazy {
		LatestSeriesRecyclerAdapter(binding.allSeriesButton.id, extendedFab?.id)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		initRecycler()
		listenImproveDialogSetting()

		burningSeriesViewModel.homeData.launchAndCollect {
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
			findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAllSeriesFragment())
		}

		binding.settingsButton.setOnClickListener {
			findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSettingsFragment())
		}

		extendedFabFavorite(HomeFragmentDirections.actionHomeFragmentToFavoritesFragment())
		extendedFab?.id?.let { binding.allSeriesButton.nextFocusRightId = it }
	}

	private fun listenImproveDialogSetting() = settingsViewModel.data.map { it.appearance.improveDialog }.launchAndCollect {
		if (it) {
			if (!burningSeriesViewModel.showedHelpImprove) {
				getBurningSeriesHosterCount { count ->
					burningSeriesViewModel.showedHelpImprove = true
					findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHelpImproveDialog(count))
				}
			}
		}
	}
	
	private fun initRecycler(): Unit = with(binding) {
		latestEpisodeRecycler.layoutManager = LinearLayoutManager(safeContext)
		latestEpisodeRecycler.adapter = latestEpisodeRecyclerAdapter
		latestEpisodeRecycler.isNestedScrollingEnabled = false
		
		latestEpisodeRecyclerAdapter.setOnClickListener { item ->
			findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSeriesFragment(
				latestEpisode = item
			))
		}
		
		latestEpisodeRecyclerAdapter.setOnLongClickListener { item ->
			val (title, episode) = item.getEpisodeAndSeries()
			findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToOpenInBrowserDialog(
				Constants.getBurningSeriesLink(item.href),
				"$episode ${safeContext.getString(R.string.of)} \"$title"
			))
			true
		}
		
		
		latestSeriesRecycler.layoutManager = LinearLayoutManager(safeContext)
		latestSeriesRecycler.adapter = latestSeriesRecyclerAdapter
		latestSeriesRecycler.isNestedScrollingEnabled = false
		
		latestSeriesRecyclerAdapter.setOnClickListener { item ->
			findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSeriesFragment(
				latestSeries = item
			))
		}

		latestSeriesRecyclerAdapter.setOnLongClickListener { item ->
			findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToOpenInBrowserDialog(
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
	}
}