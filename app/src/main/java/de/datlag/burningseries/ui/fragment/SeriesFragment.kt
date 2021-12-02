package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ahmed3elshaer.selectionbottomsheet.SelectionBuilder
import com.devs.readmoreoption.ReadMoreOption
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.EpisodeRecyclerAdapter
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentSeriesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.coilifier.PlaceholderScaling
import de.datlag.coilifier.Scale
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.series.SeasonData
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
@Obfuscate
class SeriesFragment : AdvancedFragment(R.layout.fragment_series) {

    private val navArgs: SeriesFragmentArgs by navArgs()
    private val binding: FragmentSeriesBinding by viewBinding()
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()

    private val episodeRecyclerAdapter = EpisodeRecyclerAdapter()
    private lateinit var readMoreOption: ReadMoreOption
    private var isLoading: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()

        readMoreOption = safeContext.readMoreOption {
            textLength(3)
            textLengthType(ReadMoreOption.TYPE_LINE)
            moreLabel("\tmore")
            lessLabel("\tless")
            labelUnderLine(true)
            moreLabelColor(safeContext.getColorCompat(R.color.defaultContentColor))
            lessLabelColor(safeContext.getColorCompat(R.color.defaultContentColor))
            expandAnimation(true)
        }

        navArgs.latestEpisode?.let { episode ->
            burningSeriesViewModel.getSeriesData(episode).launchAndCollect {
                seriesFlowCollect(it)
            }
        }
        navArgs.latestSeries?.let { latest ->
            burningSeriesViewModel.getSeriesData(latest).launchAndCollect {
                seriesFlowCollect(it)
            }
        }
        navArgs.seriesWithInfo?.let {
            setViewData(it)
        }
        (navArgs.genreItem as? GenreModel.GenreItem?)?.let { item ->
            Timber.e(item.toString())
            burningSeriesViewModel.getSeriesData(item).launchAndCollect {
                seriesFlowCollect(it)
            }
        }
    }

    private fun seriesFlowCollect(it: Resource<SeriesWithInfo?>) {
        when (it.status) {
            Resource.Status.LOADING -> {
                isLoading = true
                it.data?.let { data -> setViewData(data) }
                statusBarAlert?.hide {
                    statusBarAlert?.setAutoHide(false)
                    statusBarAlert?.setText("Loading...")
                    statusBarAlert?.showProgress()
                    statusBarAlert?.show()
                }
            }
            Resource.Status.SUCCESS -> {
                it.data?.let { data -> setViewData(data) }
                if (isLoading) {
                    isLoading = false
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
            }
            Resource.Status.ERROR -> {
                if (isLoading) {
                    isLoading = false
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
    }

    private fun initRecycler(): Unit = with(binding) {
        episodeRecyclerAdapter.submitList(listOf())
        episodeRecycler.layoutManager = LinearLayoutManager(safeContext)
        episodeRecycler.adapter = episodeRecyclerAdapter
        episodeRecycler.isNestedScrollingEnabled = false

        episodeRecyclerAdapter.setOnClickListener { _, item ->
            burningSeriesViewModel.getStream(item.hoster).launchAndCollect {
                when (it.status) {
                    Resource.Status.LOADING -> { /* loading indicator */ }
                    Resource.Status.ERROR -> {
                        Timber.e("No video available")
                    }
                    Resource.Status.SUCCESS -> {
                        val list = it.data!!
                        Timber.e(list.toString())
                    }
                }
            }
        }
    }

    private fun setViewData(seriesData: SeriesWithInfo): Unit = with(binding) {
        loadImageAndSave(Constants.getBurningSeriesLink(seriesData.series.image)) { bytes ->
            banner.load<Drawable>(bytes) {
                scaleType(Scale.FIT_CENTER)
                banner.drawable?.let { placeholder(it, PlaceholderScaling.fitCenter()) }
            }
        }
        title.text = seriesData.series.title
        selectLanguage.text = seriesData.languages.getOrNull(0)?.text ?: "Languages"
        selectSeason.text = seriesData.series.season
        if (seriesData.seasons.isLargerThan(1)) {
            selectSeason.show()
        } else {
            selectSeason.hide()
        }
        episodeRecyclerAdapter.submitList(seriesData.episodes)
        readMoreOption.addReadMoreTo(description, seriesData.series.description)
        favIconColorApply(seriesData.series.favoriteSince > 0L)

        favIcon.setOnClickListener {
            val isFav = seriesData.series.favoriteSince > 0L
            seriesData.series.apply {
                favoriteSince = if (!isFav) Clock.System.now().epochSeconds else 0L
            }
            burningSeriesViewModel.updateSeriesFavorite(seriesData)

            favIconColorApply(!isFav)
        }

        selectSeason.setOnClickListener {
            SelectionBuilder<SeasonData>(safeActivity as AppCompatActivity)
                .list(seriesData.seasons)
                .title("Select Season")
                .itemBinder { it.title }
                .show {
                    Timber.e("Selected: $it")
                }
        }
    }

    private fun favIconColorApply(isFav: Boolean): Unit = with(binding) {
        if (isFav) {
            favIcon.load<Drawable>(R.drawable.ic_baseline_favorite_24) {
                scaleType(Scale.CENTER_INSIDE)
            }
            favIcon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                safeContext.getColorCompat(R.color.favIconColorTrue),
                BlendModeCompat.SRC_IN
            )
        } else {
            favIcon.load<Drawable>(R.drawable.ic_outline_favorite_border_24) {
                scaleType(Scale.CENTER_INSIDE)
            }
            favIcon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                safeContext.getColorCompat(R.color.favIconColorFalse),
                BlendModeCompat.SRC_IN
            )
        }
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
    }
}