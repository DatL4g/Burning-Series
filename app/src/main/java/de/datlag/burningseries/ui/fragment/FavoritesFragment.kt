package de.datlag.burningseries.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.databinding.FragmentFavoritesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber

@AndroidEntryPoint
@Obfuscate
class FavoritesFragment : AdvancedFragment(R.layout.fragment_favorites) {

    private val binding: FragmentFavoritesBinding by viewBinding()
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        extendedFab?.let { fab ->
            fab.hide()
        }

        burningSeriesViewModel.favorites.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                Timber.e("No favorites")
            } else {
                list.map { it.toString() }.forEach(Timber::e)
            }
        }
    }
}