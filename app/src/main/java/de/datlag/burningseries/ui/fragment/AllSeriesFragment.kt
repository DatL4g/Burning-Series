package de.datlag.burningseries.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.AllSeriesRecyclerAdapter
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentAllSeriesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.GenreItem
import io.michaelrocks.paranoid.Obfuscate

@AndroidEntryPoint
@Obfuscate
class AllSeriesFragment : AdvancedFragment(R.layout.fragment_all_series) {

    private val navArgs: AllSeriesFragmentArgs by navArgs()
    private val binding: FragmentAllSeriesBinding by viewBinding()
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()

    private val allSeriesRecyclerAdapter by lazy {
        AllSeriesRecyclerAdapter(extendedFab?.id, extendedFab?.id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()
        initSearchView()

        navArgs.defaultGenre?.let { default ->
            val currentGenres = burningSeriesViewModel.genres
            val genresIndex = currentGenres.indexOfFirst { it == default || it.genre.equals(default.genre, true) }
            if (genresIndex >= 0) {
                burningSeriesViewModel.setAllSeriesPage(genresIndex)
            }
        }

        nextFab?.setOnClickListener {
            burningSeriesViewModel.getAllSeriesNext()
        }
        previousFab?.setOnClickListener {
            burningSeriesViewModel.getAllSeriesPrevious()
        }

        collectPagination()
        collectPaginatedData()
        collectSeriesCount()
    }

    private fun collectPagination() = burningSeriesViewModel.allSeriesPagination.launchAndCollect {
        binding.allSeriesRecycler.gone()
        binding.loadingView.visible()
        burningSeriesViewModel.getNewPaginationData()
    }

    private fun collectPaginatedData() = burningSeriesViewModel.allSeriesPaginatedFlat.launchAndCollect {
        if (it.second.isEmpty() && burningSeriesViewModel.allSeriesCount.value == 0L) {
            binding.allSeriesRecycler.gone()
            binding.loadingView.visible()
        } else {
            if (it.first) {
                binding.allSeriesRecycler.gone()
                binding.loadingView.visible()
            }
            allSeriesRecyclerAdapter.submitList(it.second) {
                binding.allSeriesRecycler.visible()
                binding.loadingView.gone()
                binding.allSeriesRecycler.smoothScrollToPosition(0)
            }
        }
    }

    private fun collectSeriesCount() = burningSeriesViewModel.allSeriesCount.launchAndCollect {
        if (it == 0L) {
            binding.allSeriesRecycler.gone()
            binding.loadingView.visible()
        }
    }

    private fun initRecycler(): Unit = with(binding) {
        allSeriesRecycler.isNestedScrollingEnabled = false
        allSeriesRecycler.adapter = allSeriesRecyclerAdapter

        allSeriesRecyclerAdapter.setOnClickListener { item ->
            if (item is GenreItem) {
                findNavController().safeNavigate(AllSeriesFragmentDirections.actionAllSeriesFragmentToSeriesFragment(genreItem = item))
            }
        }
        allSeriesRecyclerAdapter.setOnLongClickListener { item ->
            if (item is GenreItem) {
                materialDialogBuilder {
                    setPositiveButtonIcon(R.drawable.ic_baseline_arrow_outward_24)
                    setNegativeButtonIcon(R.drawable.ic_baseline_close_24)
                    builder {
                        setTitle(R.string.open_in_browser)
                        setMessage(safeContext.getString(R.string.open_in_browser_text, item.title))
                        setPositiveButton(R.string.open) { dialog, _ ->
                            dialog.dismiss()
                            Constants.getBurningSeriesLink(item.href).toUri().openInBrowser(safeContext)
                        }
                        setNegativeButton(R.string.close) { dialog, _ ->
                            dialog.cancel()
                        }
                    }
                }.show()
            }
            true
        }
    }

    private fun initSearchView() {
        searchView?.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty()) {
                    burningSeriesViewModel.searchAllSeries(newText)
                }
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                burningSeriesViewModel.getNewPaginationData()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                burningSeriesViewModel.searchAllSeries(query)
                return false
            }
        })
        searchView?.setKeepQuery(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.all_series_menu, menu)
        val item = menu.findItem(R.id.action_search)
        searchView?.setMenuItem(item)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        burningSeriesViewModel.cancelFetchSeries()
        burningSeriesViewModel.setSeriesData(null)
    }

    override fun onDestroyView() {
        burningSeriesViewModel.cancelSearch()
        super.onDestroyView()
    }

    override fun initActivityViews() {
        super.initActivityViews()

        exitFullScreen()
        hideSeriesArc()
        extendedFabFavorite(AllSeriesFragmentDirections.actionAllSeriesFragmentToFavoritesFragment())
        showNavigationFabs()
        setHasOptionsMenu(true)
        showToolbarBackButton()
        hideSeriesArc()
        setToolbarTitle(R.string.all_series)
        appBarLayout?.setExpanded(false, false)
        appBarLayout?.setExpandable(false)
    }
}