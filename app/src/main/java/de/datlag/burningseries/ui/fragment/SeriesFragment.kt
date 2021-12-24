package de.datlag.burningseries.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ahmed3elshaer.selectionbottomsheet.ExpandState
import com.ahmed3elshaer.selectionbottomsheet.selectionBottomSheet
import com.devs.readmoreoption.ReadMoreOption
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.EpisodeRecyclerAdapter
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentSeriesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.burningseries.viewmodel.VideoViewModel
import de.datlag.coilifier.Scale
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.series.LanguageData
import de.datlag.model.burningseries.series.SeasonData
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.model.jsonbase.Stream
import de.datlag.model.video.VideoStream
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.datetime.Clock

@AndroidEntryPoint
@Obfuscate
class SeriesFragment : AdvancedFragment(R.layout.fragment_series) {

    private val navArgs: SeriesFragmentArgs by navArgs()
    private val binding: FragmentSeriesBinding by viewBinding()
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()
    private val videoViewModel: VideoViewModel by viewModels()

    private val episodeRecyclerAdapter = EpisodeRecyclerAdapter()
    private lateinit var readMoreOption: ReadMoreOption

    private lateinit var currentSeriesWithInfo: SeriesWithInfo

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()

        readMoreOption = safeContext.readMoreOption {
            textLength(3)
            textLengthType(ReadMoreOption.TYPE_LINE)
            moreLabel("\t${safeContext.getString(R.string.more)}")
            lessLabel("\t${safeContext.getString(R.string.less)}")
            labelUnderLine(true)
            moreLabelColor(safeContext.getColorCompat(R.color.defaultContentColor))
            lessLabelColor(safeContext.getColorCompat(R.color.defaultContentColor))
            expandAnimation(true)
        }

        navArgs.latestEpisode?.let { episode ->
            burningSeriesViewModel.getSeriesData(episode).launchAndCollect {
                seriesFlowCollect(it, episode)
            }
        }
        navArgs.latestSeries?.let { latest ->
            burningSeriesViewModel.getSeriesData(latest).launchAndCollect {
                seriesFlowCollect(it)
            }
        }
        navArgs.seriesWithInfo?.let { series ->
            setViewData(series)
            burningSeriesViewModel.getSeriesData(series.series.href, series.series.hrefTitle).launchAndCollect {
                seriesFlowCollect(it)
            }
        }
        (navArgs.genreItem as? GenreModel.GenreItem?)?.let { item ->
            burningSeriesViewModel.getSeriesData(item).launchAndCollect {
                seriesFlowCollect(it)
            }
        }
    }

    private fun seriesFlowCollect(it: Resource<SeriesWithInfo?>, episode: LatestEpisode? = null) {
        when (it.status) {
            Resource.Status.LOADING -> {
                it.data?.let { data -> setViewData(data, episode) }
                showLoadingStatusBar()
                showLoadingDialog()
            }
            Resource.Status.SUCCESS -> {
                it.data?.let { data -> setViewData(data, episode) }
                showSuccessStatusBar()
                hideLoadingDialog()
            }
            Resource.Status.ERROR -> {
                showErrorStatusBar()
                hideLoadingDialog()
            }
        }
    }

    private fun initRecycler(): Unit = with(binding) {
        episodeRecyclerAdapter.submitList(listOf())
        episodeRecycler.layoutManager = LinearLayoutManager(safeContext)
        episodeRecycler.adapter = episodeRecyclerAdapter
        episodeRecycler.isNestedScrollingEnabled = false

        episodeRecyclerAdapter.setOnClickListener { item ->
            burningSeriesViewModel.getStream(item.hoster).launchAndCollect {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showLoadingDialog()
                    }
                    Resource.Status.ERROR -> {
                        hideLoadingDialog()
                        findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToNoStreamSourceDialog(
                            currentSeriesWithInfo,
                            item.episode.href
                        ))
                    }
                    Resource.Status.SUCCESS -> {
                        val list = it.data ?: listOf()
                        if (list.isEmpty()) {
                            hideLoadingDialog()
                            findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToNoStreamSourceDialog(
                                currentSeriesWithInfo,
                                item.episode.href
                            ))
                        } else {
                            getVideoSources(item.episode.title, item.episode.href, list)
                        }
                    }
                }
            }
        }

        episodeRecyclerAdapter.setOnLongClickListener { item ->
            findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToOpenInBrowserDialog(
                Constants.getBurningSeriesLink(item.episode.href),
                item.episode.title,
                currentSeriesWithInfo
            ))
            true
        }
    }

    private fun setViewData(seriesData: SeriesWithInfo, episode: LatestEpisode? = null): Unit = with(binding) {
        hideLoadingDialog()
        currentSeriesWithInfo = seriesData
        loadImageAndSave(Constants.getBurningSeriesLink(seriesData.series.image)) { bytes ->
            binding.banner.load<Drawable>(bytes) {
                scaleType(Scale.FIT_CENTER)
            }
        }
        title.text = seriesData.series.title

        val defaultLanguage = seriesData.languages.find {
            it.value == seriesData.series.selectedLanguage
        } ?: seriesData.languages.getOrNull(0)
        val defaultSeason = seriesData.series.season

        selectLanguage.text = defaultLanguage?.text ?: safeContext.getString(R.string.language)
        selectSeason.text = defaultSeason
        if (seriesData.seasons.isLargerThan(1)) {
            selectSeason.show()
        } else {
            selectSeason.hide()
        }
        episodeRecyclerAdapter.submitList(seriesData.episodes) {
            if (episode != null) {
                val (title, episodeTitle) = episode.getEpisodeAndSeries()
                episodeRecyclerAdapter.performClickOn {
                    it.episode.href.equals(episode.href, true) || it.episode.title == episodeTitle
                }
            }
        }

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

        selectLanguage.isEnabled = seriesData.languages.isLargerThan(1)
        selectLanguage.setOnClickListener { _ ->
            selectionBottomSheet<LanguageData> {
                dragIndicatorColor(getCompatColor(R.color.defaultContentColor))
                title(safeContext.getString(R.string.select_language))
                titleColor(getCompatColor(R.color.defaultContentColor))
                list(seriesData.languages)
                itemBinder { it.text }
                defaultItem { it == defaultLanguage }
                itemColor(getCompatColor(R.color.defaultContentColor))
                selectionColor(getCompatColor(R.color.defaultContentColor))
                selectionDrawable(getCompatDrawable(R.drawable.ic_baseline_language_24))
                confirmDisabledBackgroundColor(getCompatColor(android.R.color.transparent))
                confirmDisabledTextColor(getCompatColor(R.color.defaultContentColor))
                confirmBackgroundColor(getCompatColor(R.color.defaultContentColor))
                confirmTextColor(getCompatColor(R.color.defaultBackgroundColor))
                confirmText(safeContext.getString(R.string.confirm))
                setExpandState(ExpandState.ExpandOnTv)
                confirmListener { selected ->
                    if (selected != null) {
                        val newHref = seriesData.series.hrefBuilder(
                            seriesData.series.currentSeason(seriesData.seasons),
                            selected.value
                        )
                        burningSeriesViewModel.getSeriesData(newHref, seriesData.series.hrefTitle, true).launchAndCollect { newSeries ->
                            seriesFlowCollect(newSeries)
                        }
                    }
                }
            }
        }

        selectSeason.setOnClickListener {
            selectionBottomSheet<SeasonData> {
                dragIndicatorColor(getCompatColor(R.color.defaultContentColor))
                title(safeContext.getString(R.string.select_season))
                titleColor(getCompatColor(R.color.defaultContentColor))
                list(seriesData.seasons)
                itemBinder {
                    val intOrNull = it.title.toIntOrNull()
                    if (intOrNull != null) {
                        safeContext.getString(R.string.season_placeholder, intOrNull)
                    } else {
                        it.title
                    }
                }
                defaultItem {
                    it.title == defaultSeason || it.title == seriesData.series.currentSeason(seriesData.seasons)
                }
                itemColor(getCompatColor(R.color.defaultContentColor))
                selectionColor(getCompatColor(R.color.defaultContentColor))
                selectionDrawable(getCompatDrawable(R.drawable.ic_baseline_video_library_24))
                confirmDisabledBackgroundColor(getCompatColor(android.R.color.transparent))
                confirmDisabledTextColor(getCompatColor(R.color.defaultContentColor))
                confirmBackgroundColor(getCompatColor(R.color.defaultContentColor))
                confirmTextColor(getCompatColor(R.color.defaultBackgroundColor))
                confirmText(safeContext.getString(R.string.confirm))
                setExpandState(ExpandState.ExpandOnTv)
                confirmListener { selected ->
                    if (selected != null) {
                        val newHref = seriesData.series.hrefBuilder(
                            selected.title,
                            seriesData.series.selectedLanguage
                        )
                        burningSeriesViewModel.getSeriesData(newHref, seriesData.series.hrefTitle, true).launchAndCollect { newSeries ->
                            seriesFlowCollect(newSeries)
                        }
                    }
                }
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

    private fun getVideoSources(title: String, bsUrl: String, list: List<Stream>) {
        videoViewModel.getVideoSources(list).launchAndCollect {
            hideLoadingDialog()
            if (it.isEmpty()) {
                findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToNoStreamSourceDialog(
                    currentSeriesWithInfo,
                    bsUrl
                ))
            } else {
                selectionBottomSheet<VideoStream> {
                    dragIndicatorColor(getCompatColor(R.color.defaultContentColor))
                    title(safeContext.getString(R.string.select_hoster))
                    titleColor(getCompatColor(R.color.defaultContentColor))
                    list(it)
                    itemBinder { item -> item.hoster }
                    confirmText(safeContext.getString(R.string.watch))
                    defaultItemFirst()
                    defaultItemConfirmable()
                    itemColor(getCompatColor(R.color.defaultContentColor))
                    selectionColor(getCompatColor(R.color.defaultContentColor))
                    selectionDrawable(getCompatDrawable(R.drawable.ic_baseline_play_arrow_24))
                    confirmDisabledBackgroundColor(getCompatColor(android.R.color.transparent))
                    confirmDisabledTextColor(getCompatColor(R.color.defaultContentColor))
                    confirmBackgroundColor(getCompatColor(R.color.defaultContentColor))
                    confirmTextColor(getCompatColor(R.color.defaultBackgroundColor))
                    setExpandState(ExpandState.ExpandOnTv)
                    confirmListener { item ->
                        if (item != null) {
                            findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToVideoFragment(item, title, bsUrl, currentSeriesWithInfo))
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
    }
}