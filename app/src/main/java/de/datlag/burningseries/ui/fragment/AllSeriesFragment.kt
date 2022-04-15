package de.datlag.burningseries.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dolatkia.animatedThemeManager.AppTheme
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.AllSeriesRecyclerAdapter
import de.datlag.burningseries.common.hideLoadingDialog
import de.datlag.burningseries.common.showLoadingDialog
import de.datlag.burningseries.databinding.FragmentAllSeriesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.GenreModel
import io.michaelrocks.paranoid.Obfuscate

@AndroidEntryPoint
@Obfuscate
class AllSeriesFragment : AdvancedFragment() {

    private val navArgs: AllSeriesFragmentArgs by navArgs()
    private val binding: FragmentAllSeriesBinding by viewBinding(CreateMethod.INFLATE)
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()

    private val allSeriesRecyclerAdapter by lazy {
        AllSeriesRecyclerAdapter(extendedFab?.id, extendedFab?.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()
        initSearchView()

        (navArgs.defaultGenre as? GenreModel.GenreData?)?.let { default ->
            val currentGenres = burningSeriesViewModel.genres
            val genresIndex = currentGenres.indexOfFirst { it == default || it.genre.equals(default.genre, true) }
            if (genresIndex >= 0) {
                burningSeriesViewModel.setAllSeriesPage(genresIndex)
            }
        }

        burningSeriesViewModel.allSeriesPagination.launchAndCollect {
            showLoadingDialog()
            burningSeriesViewModel.getNewPaginationData()
        }
        burningSeriesViewModel.allSeriesPaginatedFlat.launchAndCollect {
            if (it.isEmpty() && burningSeriesViewModel.allSeriesCount.value == 0L) {
                showLoadingDialog()
            } else {
                showLoadingDialog()
                allSeriesRecyclerAdapter.submitList(it) {
                    hideLoadingDialog()
                }
                binding.allSeriesRecycler.smoothScrollToPosition(0)
            }
        }
        burningSeriesViewModel.allSeriesCount.launchAndCollect {
            if (it == 0L) {
                showLoadingDialog()
            }
        }
        nextFab?.setOnClickListener {
            burningSeriesViewModel.getAllSeriesNext()
        }
        previousFab?.setOnClickListener {
            burningSeriesViewModel.getAllSeriesPrevious()
        }
    }

    override fun syncTheme(appTheme: AppTheme) { }

    private fun initRecycler(): Unit = with(binding) {
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
        burningSeriesViewModel.cancelFetchSeries()
        burningSeriesViewModel.setSeriesData(null)
    }

    override fun onStop() {
        super.onStop()
        setSupportActionBar(null)
    }
}