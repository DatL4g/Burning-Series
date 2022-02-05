package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.LatestEpisodeRecyclerAdapter
import de.datlag.burningseries.adapter.LatestSeriesRecyclerAdapter
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.FragmentHomeBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.burningseries.viewmodel.GitHubViewModel
import de.datlag.burningseries.viewmodel.SettingsViewModel
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
@Obfuscate
class HomeFragment : AdvancedFragment(R.layout.fragment_home) {
	
	private val binding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::bind)
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
		listenNewVersionDialog()

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
		} else {
			burningSeriesViewModel.showedHelpImprove = true
		}
	}

	private fun listenNewVersionDialog() = gitHubViewModel.getLatestRelease().launchAndCollect {
		if (!gitHubViewModel.showedNewVersion && it != null && burningSeriesViewModel.showedHelpImprove) {
			gitHubViewModel.showedNewVersion = true
		}
	}
	
	private fun initRecycler(): Unit = with(binding) {
		latestEpisodeRecycler.adapter = latestEpisodeRecyclerAdapter
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
		

		latestSeriesRecycler.adapter = latestSeriesRecyclerAdapter
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