package de.datlag.burningseries.ui.fragment

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ahmed3elshaer.selectionbottomsheet.ExpandState
import com.ahmed3elshaer.selectionbottomsheet.selectionBottomSheet
import com.devs.readmoreoption.ReadMoreOption
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeManager
import com.google.android.material.chip.Chip
import com.hadiyarajesh.flower.Resource
import com.kttdevelopment.mal4j.anime.AnimePreview
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.EpisodeRecyclerAdapter
import de.datlag.burningseries.adapter.SeriesInfoAdapter
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentSeriesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.burningseries.viewmodel.SettingsViewModel
import de.datlag.burningseries.viewmodel.UserViewModel
import de.datlag.burningseries.viewmodel.VideoViewModel
import de.datlag.coilifier.Scale
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.JaroWinkler
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.series.EpisodeInfo
import de.datlag.model.burningseries.series.InfoData
import de.datlag.model.burningseries.series.LanguageData
import de.datlag.model.burningseries.series.SeasonData
import de.datlag.model.burningseries.series.relation.EpisodeWithHoster
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.model.jsonbase.Stream
import de.datlag.model.video.VideoStream
import de.datlag.network.anilist.MediaQuery
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

@AndroidEntryPoint
@Obfuscate
class SeriesFragment : AdvancedFragment() {

    private val navArgs: SeriesFragmentArgs by navArgs()
    private val binding: FragmentSeriesBinding by viewBinding(CreateMethod.INFLATE)
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val videoViewModel: VideoViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private val episodeRecyclerAdapter = EpisodeRecyclerAdapter()
    private val seriesInfoAdapter = SeriesInfoAdapter()
    private lateinit var readMoreOption: ReadMoreOption

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
        recoverMalAuthState()
        recoverAniListAuthState()

        listenSeriesStatus()
        listenSeriesData()
        listenCover()
        listenTitle()
        listenIsFavorite()
        listenSelectedLanguage()
        listenLanguages()
        listenSelectedSeason()
        listenSeasons()
        listenDescription()
        listenInfo()

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
            burningSeriesViewModel.getSeriesData(episode)
            listenEpisodes(episode)
        }
        navArgs.latestSeries?.let { latest ->
            burningSeriesViewModel.getSeriesData(latest)
            listenEpisodes()
        }
        navArgs.seriesWithInfo?.let { series ->
            burningSeriesViewModel.setSeriesData(series)
            burningSeriesViewModel.getSeriesData(series.series.href, series.series.hrefTitle)
            listenEpisodes()
        }
        (navArgs.genreItem as? GenreModel.GenreItem?)?.let { item ->
            burningSeriesViewModel.getSeriesData(item)
            listenEpisodes()
        }
    }

    override fun syncTheme(appTheme: AppTheme) {
        val currentTheme = appTheme as? ApplicationTheme?
        currentTheme?.let {
            binding.parent.setBackgroundColor(it.defaultBackgroundColor(safeContext))
            binding.title.setTextColor(it.defaultContentColor(safeContext))
            binding.selectLanguage.setTextColor(it.buttonTransparentTextStateList(safeContext))
            binding.selectLanguage.backgroundTintList = it.buttonTransparentBackgroundStateList(safeContext)
            binding.selectSeason.setTextColor(it.defaultBackgroundColor(safeContext))
            binding.selectSeason.setBackgroundColor(it.defaultContentColor(safeContext))
            binding.description.setTextColor(it.defaultContentColor(safeContext))
        }
    }

    private fun seriesLanguageSelector(languageData: LanguageData) {
        selectionBottomSheet<LanguageData> {
            dragIndicatorColor(getCompatColor(R.color.defaultContentColor))
            title(safeContext.getString(R.string.select_language))
            titleColor(getCompatColor(R.color.defaultContentColor))
            list(burningSeriesViewModel.currentSeriesLanguages)
            itemBinder { item -> item.text }
            defaultItem { item -> item == languageData || item.text.equals(languageData.text, true) }
            itemColor(getCompatColor(R.color.defaultContentColor))
            selectionColor(getCompatColor(R.color.defaultContentColor))
            selectionDrawable(getCompatDrawable(R.drawable.ic_baseline_language_24))
            confirmDisabledBackgroundColor(getCompatColor(android.R.color.transparent))
            confirmDisabledTextColor(getCompatColor(R.color.defaultContentColor))
            confirmBackgroundColor(getCompatColor(R.color.defaultContentColor))
            confirmTextColor(getCompatColor(R.color.defaultBackgroundColor))
            confirmText(safeContext.getString(R.string.confirm))
            setExpandState(ExpandState.ExpandCustom { isTvOrLandscape() })
            confirmListener { selected ->
                if (selected != null) {
                    val seriesData = burningSeriesViewModel.currentSeriesData?.series
                    val newHref = seriesData?.currentSeason(burningSeriesViewModel.currentSeriesSeasons)?.let { season ->
                        seriesData.hrefBuilder(
                            season.value,
                            selected.value
                        )
                    }
                    if (newHref != null) {
                        burningSeriesViewModel.getSeriesData(newHref, seriesData.hrefTitle, true)
                    }
                }
            }
        }.show(this)
    }

    private fun seriesSeasonSelector(seasonData: SeasonData) {
        selectionBottomSheet<SeasonData> {
            dragIndicatorColor(getCompatColor(R.color.defaultContentColor))
            title(safeContext.getString(R.string.select_season))
            titleColor(getCompatColor(R.color.defaultContentColor))
            list(burningSeriesViewModel.currentSeriesSeasons)
            itemBinder {
                val intOrNull = it.title.toIntOrNull()
                if (intOrNull != null) {
                    safeContext.getString(R.string.season_placeholder, intOrNull)
                } else {
                    it.title
                }
            }
            defaultItem {
                it.title.equals(seasonData.title, true) || it.title.equals(burningSeriesViewModel.currentSeriesData?.series?.currentSeason(burningSeriesViewModel.currentSeriesSeasons)?.title, true)
            }
            itemColor(getCompatColor(R.color.defaultContentColor))
            selectionColor(getCompatColor(R.color.defaultContentColor))
            selectionDrawable(getCompatDrawable(R.drawable.ic_baseline_video_library_24))
            confirmDisabledBackgroundColor(getCompatColor(android.R.color.transparent))
            confirmDisabledTextColor(getCompatColor(R.color.defaultContentColor))
            confirmBackgroundColor(getCompatColor(R.color.defaultContentColor))
            confirmTextColor(getCompatColor(R.color.defaultBackgroundColor))
            confirmText(safeContext.getString(R.string.confirm))
            setExpandState(ExpandState.ExpandCustom { isTvOrLandscape() })
            confirmListener { selected ->
                if (selected != null) {
                    val seriesData = burningSeriesViewModel.currentSeriesData?.series
                    val newHref = seriesData?.hrefBuilder(
                        selected.value,
                        seriesData.selectedLanguage
                    )
                    if (newHref != null) {
                        burningSeriesViewModel.getSeriesData(newHref, seriesData.hrefTitle, true)
                    }
                }
            }
        }.show(this)
    }

    private fun initRecycler(): Unit = with(binding) {
        infoRecycler.itemAnimator = null
        seriesInfoAdapter.submitList(listOf())
        infoRecycler.adapter = seriesInfoAdapter

        episodeRecycler.itemAnimator = null
        episodeRecyclerAdapter.submitList(listOf())
        episodeRecycler.adapter = episodeRecyclerAdapter

        episodeRecyclerAdapter.setOnClickListener { item ->
            burningSeriesViewModel.getStream(item.hoster).launchAndCollect {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showLoadingDialog()
                    }
                    Resource.Status.ERROR -> {
                        hideLoadingDialog()
                        findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToNoStreamSourceDialog(
                            burningSeriesViewModel.currentSeriesData!!,
                            item.episode.href
                        ))
                    }
                    Resource.Status.SUCCESS -> {
                        val list = it.data ?: listOf()
                        if (list.isEmpty()) {
                            hideLoadingDialog()
                            findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToNoStreamSourceDialog(
                                burningSeriesViewModel.currentSeriesData!!,
                                item.episode.href
                            ))
                        } else {
                            getVideoSources(item.episode, list)
                        }
                    }
                }
            }
        }

        episodeRecyclerAdapter.setOnLongClickListener { item ->
            findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToOpenInBrowserDialog(
                Constants.getBurningSeriesLink(item.episode.href),
                item.episode.title,
                burningSeriesViewModel.currentSeriesData!!
            ))
            true
        }

        favIcon.setOnClickListener { _ ->
            val emit = burningSeriesViewModel.currentSeriesData?.apply {
                series.favoriteSince = if (series.favoriteSince <= 0) Clock.System.now().epochSeconds else 0L
            }
            if (emit != null) {
                burningSeriesViewModel.updateSeriesFavorite(emit)
                favIconColorApply(emit.series.favoriteSince > 0)
            }
        }
    }

    private fun listenSeriesStatus() = burningSeriesViewModel.seriesStatus.distinctUntilChanged().launchAndCollect {
        when (it) {
            Resource.Status.LOADING -> {
                showLoadingStatusBar()
                showLoadingDialog()
            }
            Resource.Status.ERROR -> {
                showErrorStatusBar()
                hideLoadingDialog()
            }
            Resource.Status.SUCCESS -> {
                showSuccessStatusBar()
                hideLoadingDialog()
            }
        }
    }

    private fun listenSeriesData() = burningSeriesViewModel.seriesData.launchAndCollect {
        if (it != null) {
            hideLoadingDialog()
        }
    }

    private fun listenCover() = burningSeriesViewModel.seriesBSImage.launchAndCollect {
        fun listenAniListSeries(current: SeriesWithInfo) = userViewModel.getAniListSeries(current).distinctUntilChanged().launchAndCollect { series ->
            if (series != null) {
                loadAniListData(series)
            }
        }

        fun listenMalSeries(current: SeriesWithInfo) = userViewModel.getUserMal { mal ->
            userViewModel.getMalSeries(mal, current).distinctUntilChanged().launchAndCollect { preview ->
                loadMalData(preview)
            }
        }

        loadSavedImage(Constants.getBurningSeriesLink(it).substringAfterLast('/'))?.let { imageLoader ->
            binding.banner.load<Drawable>(imageLoader) {
                fitCenter()
            }
        }


        lifecycleScope.launch(Dispatchers.IO) {
            burningSeriesViewModel.currentSeriesData?.let { current ->
                listenAniListSeries(current)
                listenMalSeries(current)
            }
        }
    }

    private fun listenTitle() = burningSeriesViewModel.seriesTitle.launchAndCollect {
        binding.title.text = it
    }

    private fun listenIsFavorite() = burningSeriesViewModel.seriesFavorite.launchAndCollect { isFav ->
        favIconColorApply(isFav)
    }

    private fun listenSelectedLanguage() = burningSeriesViewModel.seriesSelectedLanguage.launchAndCollect {
        binding.selectLanguage.text = it?.text ?: safeContext.getString(R.string.language)
        binding.selectLanguage.isEnabled = burningSeriesViewModel.currentSeriesLanguages.isLargerThan(1)

        binding.selectLanguage.setOnClickListener { _ ->
            if (it != null) {
                seriesLanguageSelector(it)
            }
        }
    }

    private fun listenLanguages() = burningSeriesViewModel.seriesLanguages.launchAndCollect {
        binding.selectLanguage.isEnabled = burningSeriesViewModel.currentSeriesLanguages.isLargerThan(1)
    }

    private fun listenSelectedSeason() = burningSeriesViewModel.seriesSelectedSeason.launchAndCollect {
        binding.selectSeason.text = burningSeriesViewModel.currentSeriesData?.series?.season ?: it?.title ?: it?.value?.toString() ?: String()
        if (burningSeriesViewModel.currentSeriesSeasons.isLargerThan(1)) {
            binding.selectSeason.show()
        } else {
            binding.selectSeason.hide()
        }

        binding.selectSeason.setOnClickListener { _ ->
            if (it != null) {
                seriesSeasonSelector(it)
            }
        }
    }

    private fun listenSeasons() = burningSeriesViewModel.seriesSeasons.launchAndCollect {
        if (burningSeriesViewModel.currentSeriesSeasons.isLargerThan(1)) {
            binding.selectSeason.show()
        } else {
            binding.selectSeason.hide()
        }
    }

    private fun listenDescription() = burningSeriesViewModel.seriesDescription.launchAndCollect {
        if (it.isNotEmpty()) {
            try {
                readMoreOption.addReadMoreTo(binding.description, it)
            } catch (ignored: Exception) {
                binding.description.text = it
            }
        }
    }

    private fun listenInfo() = burningSeriesViewModel.seriesInfo.launchAndCollect {
        var genreInfo: InfoData? = null
        seriesInfoAdapter.submitList(it.mapNotNull { info ->
            if (info.header.trim().equals("Genre", true) || info.header.trim().equals("Genres", true)) {
                genreInfo = info
                null
            } else {
                info
            }
        }.sortedBy { info -> info.header.trim() })

        burningSeriesViewModel.getAllGenres()
        binding.genreGroup.removeAllViews()

        if (genreInfo != null) {
            val genreSplit = genreInfo!!.data.trim().split("\\s".toRegex())

            genreSplit.subList(0, if (genreSplit.size >= 5) 5 else genreSplit.size).forEach { genre ->
                addGenre(genre)
            }
        }
    }

    private fun addGenre(genre: String) = lifecycleScope.launch(Dispatchers.Main) {
        val applyTextColor = (ThemeManager.instance.getCurrentTheme() as? ApplicationTheme?)?.defaultBackgroundColor(safeContext) ?: safeContext.getColorCompat(R.color.defaultBackgroundColor)
        val applyBackgroundColor = (ThemeManager.instance.getCurrentTheme() as? ApplicationTheme?)?.defaultContentColor(safeContext) ?: safeContext.getColorCompat(R.color.defaultContentColor)

        binding.genreGroup.addView(Chip(safeContext).apply {
            setTextColor(applyTextColor)
            chipBackgroundColor = ColorStateList.valueOf(applyBackgroundColor)
            text = genre.trim()
            setOnClickListener {
                findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToAllSeriesFragment(bestGenre(genre)))
            }
        })
    }

    private fun bestGenre(genre: String): GenreModel.GenreData? {
        return burningSeriesViewModel.genres.firstOrNull {
            it.genre.trim().equals(genre.trim(), true)
        } ?: burningSeriesViewModel.genres.associateBy {
            JaroWinkler.distance(genre.trim(), it.genre.trim())
        }.maxByOrNull { it.key }?.value
    }

    private fun listenEpisodes(episode: LatestEpisode? = null) = burningSeriesViewModel.seriesEpisodes.launchAndCollect { episodes ->
        episodeRecyclerAdapter.submitList(episodes.sortedWith(compareBy<EpisodeWithHoster> { it.episode.number.toIntOrNull() }.thenBy { it.episode.number })) {
            if (episode != null) {
                val (_, episodeTitle) = episode.getEpisodeAndSeries()
                episodeRecyclerAdapter.performClickOn {
                    it.episode.href.equals(episode.href, true) || it.episode.title.equals(episodeTitle, true)
                }
            }
        }
    }

    private fun favIconColorApply(isFav: Boolean): Unit = with(binding) {
        if (isFav) {
            favIcon.load<Drawable>(R.drawable.ic_baseline_favorite_24) {
                scaleType(Scale.CENTER_INSIDE)
            }
            favIcon.clearTint()
            val color = (ThemeManager.instance.getCurrentTheme() as? ApplicationTheme?)?.favoriteIconCheckedColor(safeContext) ?: safeContext.getColorCompat(R.color.favIconColorTrue)
            favIcon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                color,
                BlendModeCompat.SRC_IN
            )
        } else {
            favIcon.load<Drawable>(R.drawable.ic_outline_favorite_border_24) {
                scaleType(Scale.CENTER_INSIDE)
            }
            favIcon.clearTint()
            val color = (ThemeManager.instance.getCurrentTheme() as? ApplicationTheme?)?.favoriteIconUnCheckedColor(safeContext) ?: safeContext.getColorCompat(R.color.favIconColorFalse)
            favIcon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                color,
                BlendModeCompat.SRC_IN
            )
        }
    }

    private fun getVideoSources(episode: EpisodeInfo, list: List<Stream>) {
        videoViewModel.getVideoSources(list).launchAndCollect {
            hideLoadingDialog()
            if (it.isEmpty()) {
                findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToNoStreamSourceDialog(
                    burningSeriesViewModel.currentSeriesData!!,
                    episode.href
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
                    setExpandState(ExpandState.ExpandCustom { isTvOrLandscape() })
                    confirmListener { item ->
                        if (item != null) {
                            findNavController().navigate(SeriesFragmentDirections.actionSeriesFragmentToVideoFragment(item, burningSeriesViewModel.currentSeriesData!!, episode))
                        }
                    }
                }.show(this@SeriesFragment)
            }
        }
    }

    private fun recoverMalAuthState() = settingsViewModel.data.map { it.user.malAuth }.launchAndCollect {
        userViewModel.loadMalAuth(it)
    }

    private fun recoverAniListAuthState() = settingsViewModel.data.map { it.user.anilistAuth }.launchAndCollect {
        userViewModel.loadAniListAuth(it)
    }

    private fun loadMalData(preview: AnimePreview?) = lifecycleScope.launch(Dispatchers.IO) {
        val defaultUrl = burningSeriesViewModel.currentSeriesData?.series?.image?.let {
            return@let Constants.getBurningSeriesLink(it)
        } ?: String()
        val saveName = defaultUrl.substringAfterLast('/')
        val malImageUrl = preview?.mainPicture?.largeURL

        fun loadImageFallback() {
            loadImageAndSave(defaultUrl, saveName) { bytes ->
                binding.banner.load<Drawable>(bytes) {
                    fitCenter()
                }
            }
        }

        if (settingsViewModel.data.map { settings -> settings.user.malImages }.first() && userViewModel.isMalAuthorized()) {
            withContext(Dispatchers.Main) {
                if (malImageUrl != null) {
                    loadImageAndSave(malImageUrl, saveName) { loader ->
                        if (loader != null) {
                            binding.banner.load<Drawable>(loader) {
                                fitCenter()
                            }
                        } else {
                            loadImageFallback()
                        }
                    }
                } else {
                    loadImageFallback()
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                loadImageFallback()
            }
        }

        preview?.let { userViewModel.syncMalSeries(
            it,
            burningSeriesViewModel.currentSeriesData!!.episodes.map { ep -> ep.episode },
            burningSeriesViewModel.currentSeriesData!!.currentSeasonIsLast
        ) }
    }

    private fun loadAniListData(medium: MediaQuery.Medium?) = lifecycleScope.launch(Dispatchers.IO) {
        val defaultUrl = Constants.getBurningSeriesLink(burningSeriesViewModel.currentSeriesData!!.series.image)
        val saveName = defaultUrl.substringAfterLast('/')
        val aniListImageUrl = medium?.coverImage?.extraLarge ?: medium?.coverImage?.large ?: medium?.coverImage?.medium

        fun loadImageFallback() {
            loadImageAndSave(defaultUrl, saveName) { bytes ->
                binding.banner.load<Drawable>(bytes) {
                    fitCenter()
                }
            }
        }

        if (settingsViewModel.data.map { settings -> settings.user.aniListImages }.first() && userViewModel.isAniListAuthorized()) {
            withContext(Dispatchers.Main) {
                if (aniListImageUrl != null) {
                    loadImageAndSave(aniListImageUrl, saveName) { loader ->
                        if (loader != null) {
                            binding.banner.load<Drawable>(loader) {
                                fitCenter()
                            }
                        } else {
                            loadImageFallback()
                        }
                    }
                } else {
                    loadImageFallback()
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                loadImageFallback()
            }
        }

        medium?.let {
            userViewModel.syncAniListSeries(
                it,
                burningSeriesViewModel.currentSeriesData!!.episodes.map { ep -> ep.episode },
                burningSeriesViewModel.currentSeriesData!!.currentSeasonIsLast
            )
        }
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
    }
}