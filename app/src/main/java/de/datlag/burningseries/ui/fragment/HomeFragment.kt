package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.LatestEpisodeRecyclerAdapter
import de.datlag.burningseries.adapter.LatestSeriesRecyclerAdapter
import de.datlag.burningseries.common.openInBrowser
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.FragmentHomeBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
@Obfuscate
class HomeFragment : AdvancedFragment(R.layout.fragment_home) {
	
	private val binding: FragmentHomeBinding by viewBinding()
	private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()
	
	private val latestEpisodeRecyclerAdapter = LatestEpisodeRecyclerAdapter()
	private val latestSeriesRecyclerAdapter = LatestSeriesRecyclerAdapter()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		initRecycler()

		if (!burningSeriesViewModel.showedHelpImprove) {
			getBurningSeriesHosterCount {
				burningSeriesViewModel.showedHelpImprove = true
				findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHelpImproveDialog(it))
			}
		}

		burningSeriesViewModel.homeData.launchAndCollect {
			when (it.status) {
				Resource.Status.LOADING -> {
					statusBarAlert?.hide {
						statusBarAlert?.setText("Loading...")
						statusBarAlert?.showProgress()
						statusBarAlert?.show()
					}
				}
				Resource.Status.SUCCESS -> {
					latestSeriesRecyclerAdapter.submitList(it.data?.latestSeries ?: listOf())
					latestEpisodeRecyclerAdapter.submitList(it.data?.latestEpisodes ?: listOf())
					statusBarAlert?.hide {
						statusBarAlert?.setAutoHide(true)
						statusBarAlert?.setDuration(2000)
						statusBarAlert?.setText("Success")
						statusBarAlert?.setAlertColor(R.color.successBackgroundColor)
						statusBarAlert?.setTextColor(R.color.successContentColor)
						statusBarAlert?.hideProgress()
						statusBarAlert?.show()
					}
				}
				Resource.Status.ERROR -> {
					statusBarAlert?.hide {
						statusBarAlert?.setAutoHide(true)
						statusBarAlert?.setDuration(5, TimeUnit.SECONDS)
						statusBarAlert?.setText("Error. Try again later")
						statusBarAlert?.setAlertColor(R.color.errorBackgroundColor)
						statusBarAlert?.setTextColor(R.color.errorContentColor)
						statusBarAlert?.hideProgress()
						statusBarAlert?.show()
					}
				}
			}
		}

		loadImageAndSave("https://bs.to/public/images/header.png") {
			binding.banner.load<Drawable>(it)
		}

		binding.allSeriesButton.setOnClickListener {
			findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAllSeriesFragment())
		}

		extendedFabFavorite(HomeFragmentDirections.actionHomeFragmentToFavoritesFragment())
	}
	
	private fun initRecycler(): Unit = with(binding) {
		latestEpisodeRecycler.layoutManager = LinearLayoutManager(safeContext)
		latestEpisodeRecycler.adapter = latestEpisodeRecyclerAdapter
		latestEpisodeRecycler.isNestedScrollingEnabled = false
		
		latestEpisodeRecyclerAdapter.setOnClickListener { _, item ->
			findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSeriesFragment(
				latestEpisode = item
			))
		}
		
		latestEpisodeRecyclerAdapter.setOnLongClickListener { _, item ->
			openInBrowser(item)
			true
		}
		
		
		latestSeriesRecycler.layoutManager = LinearLayoutManager(safeContext)
		latestSeriesRecycler.adapter = latestSeriesRecyclerAdapter
		latestSeriesRecycler.isNestedScrollingEnabled = false
		
		latestSeriesRecyclerAdapter.setOnClickListener { _, item ->
			Timber.e(item.toString())
			findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSeriesFragment(
				latestSeries = item
			))
		}

		latestSeriesRecyclerAdapter.setOnLongClickListener { _, item ->
			openInBrowser(item)
			true
		}
	}

	override fun onResume() {
		super.onResume()
		extendedFab?.visibility = View.VISIBLE
		hideNavigationFabs()
	}
	
	private fun openInBrowser(item: LatestEpisode) {
		val (title, episode) = item.getEpisodeAndSeries()
		MaterialAlertDialogBuilder(safeContext)
			.setTitle(title)
			.setMessage("Do you want to open \"${episode}\" of \"${title}\" in browser?")
			.setPositiveButton("Yes") { _, _ ->
				Constants.getBurningSeriesLink(item.href).toUri().openInBrowser(safeContext)
			}
			.setNegativeButton("Cancel", null)
			.create().show()
	}

	private fun openInBrowser(item: LatestSeries) {
		MaterialAlertDialogBuilder(safeContext)
			.setTitle(item.title)
			.setMessage("Do you want to open \"${item.title}\" in browser?")
			.setPositiveButton("Yes") { _, _ ->
				Constants.getBurningSeriesLink(item.href).toUri().openInBrowser(safeContext)
			}
			.setNegativeButton("Cancel", null)
			.create().show()
	}
}