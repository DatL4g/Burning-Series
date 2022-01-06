package de.datlag.burningseries.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.AllSeriesRecyclerAdapter
import de.datlag.burningseries.common.hideLoadingDialog
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.common.showLoadingDialog
import de.datlag.burningseries.databinding.FragmentAllSeriesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.GenreModel
import io.michaelrocks.paranoid.Obfuscate

@AndroidEntryPoint
@Obfuscate
class AllSeriesFragment : AdvancedFragment(R.layout.fragment_all_series) {

    private val binding: FragmentAllSeriesBinding by viewBinding(FragmentAllSeriesBinding::bind)
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()

    private val allSeriesRecyclerAdapter by lazy {
        AllSeriesRecyclerAdapter(extendedFab?.id, extendedFab?.id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()
        initSearchView()
        burningSeriesViewModel.allSeriesPagination.launchAndCollect {
            showLoadingDialog()
            burningSeriesViewModel.getNewPaginationData()
        }
        burningSeriesViewModel.allSeriesPaginatedFlat.launchAndCollect {
            allSeriesRecyclerAdapter.submitList(it) {
                hideLoadingDialog()
            }
            binding.allSeriesRecycler.smoothScrollToPosition(0)
        }
        nextFab?.setOnClickListener {
            burningSeriesViewModel.getAllSeriesNext()
        }
        previousFab?.setOnClickListener {
            burningSeriesViewModel.getAllSeriesPrevious()
        }
    }

    private fun initRecycler(): Unit = with(binding) {
        allSeriesRecycler.layoutManager = LinearLayoutManager(safeContext)
        allSeriesRecycler.adapter = allSeriesRecyclerAdapter

        allSeriesRecyclerAdapter.setOnClickListener { item ->
            if (item is GenreModel.GenreItem) {
                findNavController().navigate(AllSeriesFragmentDirections.actionAllSeriesFragmentToSeriesFragment(genreItem = item))
            }
        }
        allSeriesRecyclerAdapter.setOnLongClickListener { item ->
            if (item is GenreModel.GenreItem) {
                findNavController().navigate(AllSeriesFragmentDirections.actionAllSeriesFragmentToOpenInBrowserDialog(
                    Constants.getBurningSeriesLink(item.href),
                    item.title
                ))
            }
            true
        }
    }

    private fun initSearchView(): Unit = with(binding) {
        searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.length > 3) {
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.all_series_menu, menu)
        val item = menu.findItem(R.id.action_search)
        binding.searchView.setMenuItem(item)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        extendedFabFavorite(AllSeriesFragmentDirections.actionAllSeriesFragmentToFavoritesFragment())
        showNavigationFabs()
        setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        showToolbarBackButton(binding.toolbar)
    }

    override fun onStop() {
        super.onStop()
        setSupportActionBar(null)
    }
}