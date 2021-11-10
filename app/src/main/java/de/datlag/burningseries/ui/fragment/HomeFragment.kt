package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fede987.statusbaralert.StatusBarAlert
import com.fede987.statusbaralert.utils.statusBarAlert
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.LatestEpisodeRecyclerAdapter
import de.datlag.burningseries.adapter.LatestSeriesRecyclerAdapter
import de.datlag.model.common.base64ToByteArray
import de.datlag.burningseries.common.loadFileInternal
import de.datlag.burningseries.common.openInBrowser
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.common.saveFileInternal
import de.datlag.burningseries.databinding.FragmentHomeBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.ui.connector.FragmentOptionsMenu
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.burningseries.home.LatestEpisode
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
@Obfuscate
class HomeFragment : AdvancedFragment(R.layout.fragment_home), FragmentOptionsMenu {
	
	private val binding: FragmentHomeBinding by viewBinding()
	private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()
	
	private val latestEpisodeRecyclerAdapter = LatestEpisodeRecyclerAdapter()
	private val latestSeriesRecyclerAdapter = LatestSeriesRecyclerAdapter()
	
	private var statusBarAlert: StatusBarAlert? = null
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		createStatusBarAlert()
		initRecycler()
		
		burningSeriesViewModel.homeData.observe(viewLifecycleOwner) {
			when (it.status) {
				Resource.Status.LOADING -> {
					statusBarAlert?.setText("Loading...")
					statusBarAlert?.showProgress()
					statusBarAlert?.show()
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
		
		toolbarImage?.let { image ->
			loadImageAndSave("https://bs.to/public/images/header.png") {
				it?.let { bytes ->
					image.load<Drawable>(bytes)
				}
			}
		}
	}
	
	override fun onCreateMenu(menu: Menu, inflater: MenuInflater): Boolean {
		Timber.e("inflate menu")
		toolbarSearchView?.let {
			Timber.e("SearchView is present")
			inflater.inflate(R.menu.home_menu, menu)
			val searchItem = menu.findItem(R.id.action_search)
			
			it.setMenuItem(searchItem)
		}
		
		return true
	}
	
	private fun createStatusBarAlert() {
		statusBarAlert = statusBarAlert {
			autoHide(false)
			showProgress(false)
			alertColor(R.color.defaultContentColor)
			textColor(R.color.defaultBackgroundColor)
			progressBarColor(R.color.defaultBackgroundColor)
		}
	}
	
	private fun initRecycler(): Unit = with(binding) {
		latestEpisodeRecycler.layoutManager = LinearLayoutManager(safeContext)
		latestEpisodeRecycler.adapter = latestEpisodeRecyclerAdapter
		latestEpisodeRecycler.isNestedScrollingEnabled = false
		
		latestEpisodeRecyclerAdapter.setOnClickListener { _, item ->
			Timber.e("Clicked")
			Timber.e(item.toString())
		}
		
		latestEpisodeRecyclerAdapter.setOnLongClickListener { _, item ->
			Timber.e("Long Clicked")
			openInBrowser(item)
			true
		}
		
		
		latestSeriesRecycler.layoutManager = LinearLayoutManager(safeContext)
		latestSeriesRecycler.adapter = latestSeriesRecyclerAdapter
		latestSeriesRecycler.isNestedScrollingEnabled = false
		
		latestSeriesRecyclerAdapter.setOnClickListener { _, item ->
			Timber.e(item.toString())
		}
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
}