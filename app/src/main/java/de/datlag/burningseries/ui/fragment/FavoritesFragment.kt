package de.datlag.burningseries.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.FavoriteRecyclerAdapter
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.FragmentFavoritesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import io.michaelrocks.paranoid.Obfuscate

@AndroidEntryPoint
@Obfuscate
class FavoritesFragment : AdvancedFragment(R.layout.fragment_favorites) {

    private val binding: FragmentFavoritesBinding by viewBinding()
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()

    private val favoritesRecyclerAdapter = FavoriteRecyclerAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()

        burningSeriesViewModel.favorites.launchAndCollect {
            favoritesRecyclerAdapter.submitList(it)
        }
    }

    private fun initRecycler(): Unit = with(binding) {
        favoritesRecycler.layoutManager = GridLayoutManager(safeContext, 3)
        favoritesRecycler.isNestedScrollingEnabled = false
        favoritesRecycler.adapter = favoritesRecyclerAdapter

        favoritesRecyclerAdapter.setOnClickListener { _, item ->
            findNavController().navigate(FavoritesFragmentDirections.actionFavoritesFragmentToSeriesFragment(seriesWithInfo = item))
        }
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
    }

    override fun onStop() {
        super.onStop()
        setSupportActionBar(null)
    }
}