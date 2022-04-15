package de.datlag.burningseries.ui.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dolatkia.animatedThemeManager.AppTheme
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.FavoriteRecyclerAdapter
import de.datlag.burningseries.common.isOrientation
import de.datlag.burningseries.common.isTelevision
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.FragmentFavoritesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import io.michaelrocks.paranoid.Obfuscate

@AndroidEntryPoint
@Obfuscate
class FavoritesFragment : AdvancedFragment() {

    private val binding: FragmentFavoritesBinding by viewBinding(CreateMethod.INFLATE)
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()

    private val favoritesRecyclerAdapter = FavoriteRecyclerAdapter(this)

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

        burningSeriesViewModel.favorites.launchAndCollect {
            favoritesRecyclerAdapter.submitList(it)
        }
        burningSeriesViewModel.getAllFavorites()
    }

    override fun syncTheme(appTheme: AppTheme) { }

    private fun initRecycler(): Unit = with(binding) {
        val gridSpanCount = if (isTelevision && isOrientation(Configuration.ORIENTATION_LANDSCAPE)) {
            6
        } else if (!isTelevision && isOrientation(Configuration.ORIENTATION_LANDSCAPE)) {
            4
        } else {
            2
        }
        favoritesRecycler.layoutManager = GridLayoutManager(safeContext, gridSpanCount)
        favoritesRecycler.isNestedScrollingEnabled = false
        favoritesRecycler.adapter = favoritesRecyclerAdapter

        favoritesRecyclerAdapter.setOnClickListener { item ->
            findNavController().navigate(FavoritesFragmentDirections.actionFavoritesFragmentToSeriesFragment(seriesWithInfo = item))
        }
    }

    private fun initSearchView(): Unit = with(binding) {
        searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty()) {
                    burningSeriesViewModel.searchFavorites(newText)
                } else {
                    burningSeriesViewModel.getAllFavorites()
                }
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                burningSeriesViewModel.getAllFavorites()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                burningSeriesViewModel.searchFavorites(query)
                return false
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.favorites_menu, menu)
        val item = menu.findItem(R.id.action_search)
        binding.searchView.setMenuItem(item)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
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