package de.datlag.burningseries.ui.fragment

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.devs.readmoreoption.ReadMoreOption
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.hadiyarajesh.flower.Resource
import com.kttdevelopment.mal4j.anime.AnimePreview
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.adapter.EpisodeRecyclerAdapter
import de.datlag.burningseries.adapter.SeriesInfoAdapter
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentSeriesBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.burningseries.viewmodel.SettingsViewModel
import de.datlag.burningseries.viewmodel.UserViewModel
import de.datlag.burningseries.viewmodel.VideoViewModel
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.JaroWinkler
import de.datlag.model.burningseries.Cover
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.series.EpisodeInfo
import de.datlag.model.burningseries.series.InfoData
import de.datlag.model.burningseries.series.LanguageData
import de.datlag.model.burningseries.series.SeasonData
import de.datlag.model.burningseries.series.relation.EpisodeWithHoster
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.model.burningseries.stream.Stream
import de.datlag.network.anilist.MediaQuery
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber

@AndroidEntryPoint
@Obfuscate
class SeriesFragment : AdvancedFragment(R.layout.fragment_series) {

    private val navArgs: SeriesFragmentArgs by navArgs()
    private val binding: FragmentSeriesBinding by viewBinding()
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val videoViewModel: VideoViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private val episodeRecyclerAdapter by lazy {
        EpisodeRecyclerAdapter(extendedFab?.id)
    }
    private val seriesInfoAdapter = SeriesInfoAdapter()
    private lateinit var readMoreOption: ReadMoreOption

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()
        recoverMalAuthState()
        recoverAniListAuthState()

        listenSeriesStatus()
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

    private fun seriesLanguageSelector(languageData: LanguageData) {
        // TODO("works with new deployed backend")
        val items = burningSeriesViewModel.currentSeriesLanguages.sortedBy { it.text }
        var defaultSelection = items.indexOf(languageData)
        if (defaultSelection <= -1) {
            defaultSelection = items.indexOfFirst { it.value.equals(languageData.value, true) }
        }
        if (defaultSelection <= -1) {
            defaultSelection = items.indexOfFirst { it.text.equals(languageData.text, true) }
        }
        if (defaultSelection <= -1) {
            defaultSelection = 0
        }
        var selection = defaultSelection

        materialDialogBuilder {
            setPositiveButtonIcon(R.drawable.ic_baseline_check_24)
            setNegativeButtonIcon(R.drawable.ic_baseline_close_24)
            builder {
                setTitle(R.string.select_language)
                setSingleChoiceItems(items.map { it.text }.toTypedArray(), selection) { _, i ->
                    selection = i
                }
                setPositiveButton(R.string.confirm) { dialog, _ ->
                    dialog.dismiss()
                    if (selection != defaultSelection) {
                        val seriesData = burningSeriesViewModel.currentSeriesData?.series
                        val newHref = seriesData?.currentSeason(burningSeriesViewModel.currentSeriesSeasons)?.let { season ->
                            seriesData.hrefBuilder(season.value, items[selection].value)
                        }
                        if (newHref != null) {
                            burningSeriesViewModel.getSeriesData(newHref, seriesData.hrefTitle, true)
                        }
                    }
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            }
        }.show()
    }

    private fun seriesSeasonSelector(seasonData: SeasonData) {
        // TODO("works with new deployed backend")
        val items = burningSeriesViewModel.currentSeriesSeasons.sortedWith(compareBy<SeasonData> { it.value }.thenBy { it.title.toIntOrNull() }.thenBy { it.title })
        var defaultSelection = items.indexOf(seasonData)
        if (defaultSelection <= -1) {
            defaultSelection = items.indexOfFirst { it.value == seasonData.value }
        }
        if (defaultSelection <= -1) {
            defaultSelection = items.indexOfFirst { it.title.equals(seasonData.title, true) }
        }
        if (defaultSelection <= -1) {
            defaultSelection = 0
        }
        var selection = defaultSelection

        materialDialogBuilder {
            setPositiveButtonIcon(R.drawable.ic_baseline_check_24)
            setNegativeButtonIcon(R.drawable.ic_baseline_close_24)
            builder {
                setTitle(R.string.select_season)
                setSingleChoiceItems(items.map { it.title }.toTypedArray(), defaultSelection) { _, i ->
                    selection = i
                }
                setPositiveButton(R.string.confirm) { dialog, _ ->
                    dialog.dismiss()
                    if (selection != defaultSelection) {
                        val seriesData = burningSeriesViewModel.currentSeriesData?.series
                        val newHref = seriesData?.hrefBuilder(items[selection].value, seriesData.selectedLanguage)
                        if (newHref != null) {
                            burningSeriesViewModel.getSeriesData(newHref, seriesData.hrefTitle, true)
                        }
                    }
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            }
        }.show()
    }

    private fun initRecycler(): Unit = with(binding) {
        infoRecycler.itemAnimator = null
        seriesInfoAdapter.submitList(listOf())
        infoRecycler.adapter = seriesInfoAdapter

        episodeRecycler.itemAnimator = null
        episodeRecyclerAdapter.submitList(listOf())
        episodeRecycler.adapter = episodeRecyclerAdapter

        episodeRecyclerAdapter.setOnClickListener { item ->
            episodeRecyclerClick(item)
        }

        episodeRecyclerAdapter.setOnLongClickListener { item ->
            materialDialogBuilder {
                setPositiveButtonIcon(R.drawable.ic_baseline_arrow_outward_24)
                setNegativeButtonIcon(R.drawable.ic_baseline_close_24)
                setNeutralButtonIcon(R.drawable.ic_baseline_edit_24)
                builder {
                    setTitle(R.string.open_in_browser)
                    setMessage(safeContext.getString(R.string.open_in_browser_text, item.episode.title))
                    setPositiveButton(R.string.open) { dialog, _ ->
                        dialog.dismiss()
                        item.episode.href.toUri().openInBrowser(safeContext, item.episode.title)
                    }
                    setNegativeButton(R.string.close) { dialog, _ ->
                        dialog.cancel()
                    }
                    setNeutralButton(R.string.activate) { dialog, _ ->
                        dialog.dismiss()
                        findNavController().safeNavigate(SeriesFragmentDirections.actionSeriesFragmentToScrapeHosterFragment(
                            Constants.getBurningSeriesLink(item.episode.href),
                            burningSeriesViewModel.currentSeriesData!!
                        ))
                    }
                }
            }.show()
            true
        }
    }

    private fun listenSeriesStatus() = burningSeriesViewModel.seriesStatus.distinctUntilChanged().launchAndCollect {
        when (it) {
            Resource.Status.LOADING -> {
                // TODO("show loading indicator, only one")
            }
            Resource.Status.ERROR -> {
                // TODO("show error snackbar or something")
                Snackbar.make(binding.root, "Error loading series", Snackbar.LENGTH_LONG).setAction("Retry") {
                    val seriesData = burningSeriesViewModel.currentSeriesData?.series
                    val newHref = seriesData?.currentSeason(burningSeriesViewModel.currentSeriesSeasons)?.let { season ->
                        seriesData.hrefBuilder(season.value, seriesData.selectedLanguage)
                    }
                    if (newHref != null) {
                        burningSeriesViewModel.getSeriesData(newHref, seriesData.hrefTitle, true)
                    }
                }.show()
            }
            Resource.Status.SUCCESS -> {
                // TODO("hide loading indicator")
            }
        }
    }

    private fun listenAniListSeries(current: SeriesWithInfo, onLoaded: (MediaQuery.Medium?) -> Unit) = userViewModel.getAniListSeries(current).distinctUntilChanged().launchAndCollect { series ->
        if (series != null) {
            onLoaded.invoke(series)
        }
    }

    private fun listenMalSeries(current: SeriesWithInfo, onLoaded: (AnimePreview?) -> Unit) = userViewModel.getUserMal { mal ->
        userViewModel.getMalSeries(mal, current).distinctUntilChanged().launchAndCollect { preview ->
            onLoaded.invoke(preview)
        }
    }

    private fun listenCover() = burningSeriesViewModel.seriesBSImage.launchAndCollect {
        val width = toolbarInfo?.seriesCover?.anyWidth ?: getDisplayWidth()
        val height = toolbarInfo?.seriesCover?.anyHeight ?: (getDisplayWidth().toFloat() * 1.6F).toInt()

        val errorBitmap = it.loadBase64Image(coversDir)?.let { bitmap ->
            val origWidth = bitmap.width
            val origHeight = bitmap.height
            val widthMultiplier = width.toFloat() / origWidth.toFloat()
            Bitmap.createScaledBitmap(bitmap, width, (origHeight.toFloat() * widthMultiplier).toInt(), true)
        }

        toolbarInfo?.seriesCover?.load<Drawable>(Constants.getBurningSeriesLink(it.href)) {
            fitCenter()
            error(errorBitmap ?: it.loadBlurHash {
                blurHash.execute(it.blurHash, width, height)
            })
        }

        lifecycleScope.launch(Dispatchers.IO) {
            burningSeriesViewModel.currentSeriesData?.let { current ->
                listenAniListSeries(current) { series -> loadAniListData(series) }
                listenMalSeries(current) { series -> loadMalData(series) }
            }
        }
    }

    private fun listenTitle() = burningSeriesViewModel.seriesTitle.launchAndCollect {
        setToolbarTitle(it)
    }

    private fun listenIsFavorite() = burningSeriesViewModel.seriesFavorite.launchAndCollect { isFav ->
        favIconColorApply(isFav, null)
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
            binding.selectSeason.visible()
        } else {
            binding.selectSeason.gone()
        }

        binding.selectSeason.setOnClickListener { _ ->
            if (it != null) {
                seriesSeasonSelector(it)
            }
        }
    }

    private fun listenSeasons() = burningSeriesViewModel.seriesSeasons.launchAndCollect {
        if (burningSeriesViewModel.currentSeriesSeasons.isLargerThan(1)) {
            binding.selectSeason.visible()
        } else {
            binding.selectSeason.gone()
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
        binding.genreGroup.addView(Chip(safeContext).apply {
            text = genre.trim()
            setOnClickListener {
                findNavController().safeNavigate(SeriesFragmentDirections.actionSeriesFragmentToAllSeriesFragment(bestGenre(genre)))
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
        if (episodes.isEmpty()) {
            extendedFab?.gone()
        } else {
            applyContinueFab()
            extendedFab?.let { fab ->
                fab.visible()
                binding.selectSeason.nextFocusRightId = fab.id
                fab.requestFocus()
            }
        }

        episodeRecyclerAdapter.submitList(episodes.sortedWith(compareBy<EpisodeWithHoster> { it.episode.number.toIntOrNull() }.thenBy { it.episode.number })) {
            if (episode != null) {
                val (_, episodeTitle) = episode.getEpisodeAndSeries()
                episodeRecyclerAdapter.performClickOn {
                    it.episode.href.equals(episode.href, true) || it.episode.title.equals(episodeTitle, true)
                }
            }
        }

        burningSeriesViewModel.currentSeriesData?.let { current ->
            listenMalSeries(current) { series -> syncMalData(series) }
            listenAniListSeries(current) { series -> syncAniListData(series) }
        }
    }

    private fun favIconColorApply(isFav: Boolean, item: MenuItem?) {
        val menuItem = item ?: materialToolbar?.menu?.findItem(R.id.series_favorite)
        if (isFav) {
            if (menuItem != null) {
                val favIcon = getCompatDrawable(R.drawable.ic_baseline_favorite_24)
                favIcon?.clearColorFilter()
                favIcon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    safeContext.getColorCompat(R.color.errorColor),
                    BlendModeCompat.SRC_IN
                )
                menuItem.icon = favIcon
                menuItem.title = getString(R.string.remove_from_favorites)
            }
        } else {
            if (menuItem != null) {
                val favIcon = getCompatDrawable(R.drawable.ic_outline_favorite_border_24)
                favIcon?.clearColorFilter()
                favIcon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    safeContext.getColorCompat(R.color.coloredBackgroundTextColor),
                    BlendModeCompat.SRC_IN
                )
                menuItem.icon = favIcon
                menuItem.title = getString(R.string.add_to_favorites)
            }
        }
    }

    private fun getVideoSources(episode: EpisodeInfo, list: List<Stream>) {
        videoViewModel.getVideoSources(list).launchAndCollect {
            // ToDo("remove episode loading indicator")
            val current = burningSeriesViewModel.currentSeriesData
            if (view != null && current != null) {
                if (it.isEmpty()) {
                    noStreamSourceDialog(episode.href, current)
                } else {
                    // TODO("show hoster selection, or start auto by default order in settings?!, view earlier commits for logic")
                    if (it.size == 1) {
                        findNavController().safeNavigate(SeriesFragmentDirections.actionSeriesFragmentToVideoFragment(
                            it.first(),
                            current,
                            episode
                        ))
                    } else {
                        var selection = 0

                        materialDialogBuilder {
                            setPositiveButtonIcon(R.drawable.ic_baseline_play_arrow_24)
                            setNegativeButtonIcon(R.drawable.ic_baseline_close_24)
                            setNeutralButtonIcon(R.drawable.ic_baseline_edit_24)

                            builder {
                                setTitle(R.string.select_hoster)
                                setSingleChoiceItems(it.map { item -> item.hoster }.toTypedArray(), selection) { _, i ->
                                    selection = i
                                }
                                setPositiveButton(R.string.watch) { dialog, _ ->
                                    dialog.dismiss()
                                    findNavController().safeNavigate(SeriesFragmentDirections.actionSeriesFragmentToVideoFragment(
                                        it[selection],
                                        current,
                                        episode
                                    ))
                                }
                                setNegativeButton(R.string.cancel) { dialog, _ ->
                                    dialog.cancel()
                                }
                                setNeutralButton(R.string.activate) { dialog, _ ->
                                    dialog.dismiss()
                                    findNavController().safeNavigate(SeriesFragmentDirections.actionSeriesFragmentToScrapeHosterFragment(
                                        Constants.getBurningSeriesLink(episode.href),
                                        current
                                    ))
                                }
                            }
                        }.show()
                    }
                }
            }
        }
    }

    private fun episodeRecyclerClick(item: EpisodeWithHoster) = burningSeriesViewModel.getStream(item.hoster).launchAndCollect {
        val current = burningSeriesViewModel.currentSeriesData
        if (view != null && current != null) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    // ToDo("display episode loading indicator")
                }
                Resource.Status.ERROR -> {
                    // ToDo("remove episode loading indicator")
                    Timber.e("Got ${it.message}")
                    noStreamSourceDialog(item.episode.href)
                }
                Resource.Status.SUCCESS -> {
                    val list = it.data ?: listOf()
                    Timber.e("Got $list")
                    if (list.isEmpty()) {
                        // ToDo("remove episode loading indicator")
                        noStreamSourceDialog(item.episode.href)
                    } else {
                        getVideoSources(item.episode, list)
                    }
                }
            }
        }
    }

    private fun noStreamSourceDialog(href: String, current: SeriesWithInfo = burningSeriesViewModel.currentSeriesData!!) {
        materialDialogBuilder {
            setPositiveButtonIcon(R.drawable.ic_baseline_edit_24)
            setNegativeButtonIcon(R.drawable.ic_baseline_close_24)
            builder {
                setTitle(R.string.no_stream_source)
                setMessage(R.string.no_stream_source_text)
                setPositiveButton(R.string.activate) { dialog, _ ->
                    dialog.dismiss()
                    findNavController().safeNavigate(SeriesFragmentDirections.actionSeriesFragmentToScrapeHosterFragment(
                        href,
                        current
                    ))
                }
                setNegativeButton(R.string.close) { dialog, _ ->
                    dialog.cancel()
                }
            }
        }.show()
    }

    private fun recoverMalAuthState() = settingsViewModel.data.map { it.user.malAuth }.launchAndCollect {
        userViewModel.loadMalAuth(it)
    }

    private fun recoverAniListAuthState() = settingsViewModel.data.map { it.user.anilistAuth }.launchAndCollect {
        userViewModel.loadAniListAuth(it)
    }

    private fun loadAnimeProviderImage(imageUrl: String?, defaultCover: Cover?) = lifecycleScope.launch(Dispatchers.Main) {
        // ToDo("load from imageUrl or ignore")
    }

    private fun syncMalData(preview: AnimePreview?) = lifecycleScope.launch(Dispatchers.IO) {
        preview?.let {
            val currentSeries = burningSeriesViewModel.currentSeriesData
            if (currentSeries != null) {
                userViewModel.syncMalSeries(
                    it,
                    burningSeriesViewModel.currentSeriesEpisodes.map { ep -> ep.episode },
                    burningSeriesViewModel.continueSeriesEpisode?.episode,
                    currentSeries.currentSeasonIsFirst,
                    currentSeries.currentSeasonIsLast
                )
            }
        }
    }

    private fun loadMalData(preview: AnimePreview?) = lifecycleScope.launch(Dispatchers.IO) {
        val defaultCover = burningSeriesViewModel.currentSeriesData?.cover
        val malImageUrl = if (settingsViewModel.data.map { settings -> settings.user.malImages }.first() && userViewModel.isMalAuthorized()) {
            preview?.mainPicture?.largeURL
        } else { null }

        loadAnimeProviderImage(malImageUrl, defaultCover)

        syncMalData(preview)
    }

    private fun syncAniListData(medium: MediaQuery.Medium?) = lifecycleScope.launch(Dispatchers.IO) {
        medium?.let {
            val currentSeries = burningSeriesViewModel.currentSeriesData
            if (currentSeries != null) {
                userViewModel.syncAniListSeries(
                    it,
                    burningSeriesViewModel.currentSeriesEpisodes.map { ep -> ep.episode },
                    burningSeriesViewModel.continueSeriesEpisode?.episode,
                    currentSeries.currentSeasonIsFirst,
                    currentSeries.currentSeasonIsLast
                )
            }
        }
    }

    private fun loadAniListData(medium: MediaQuery.Medium?) = lifecycleScope.launch(Dispatchers.IO) {
        val defaultCover = burningSeriesViewModel.currentSeriesData?.cover
        val aniListImageUrl = if (settingsViewModel.data.map { settings -> settings.user.aniListImages }.first() && userViewModel.isAniListAuthorized()) {
            medium?.coverImage?.extraLarge ?: medium?.coverImage?.large ?: medium?.coverImage?.medium
        } else { null }

        loadAnimeProviderImage(aniListImageUrl, defaultCover)

        syncAniListData(medium)
    }

    private fun applyContinueFab() {
        extendedFab?.let { fab ->
            val continueEpisode = burningSeriesViewModel.continueSeriesEpisode
            val continueEpisodeNumber = continueEpisode?.episode?.episodeNumberOrListNumber
            fab.visibility = if (burningSeriesViewModel.currentSeriesEpisodes.isNullOrEmpty()) View.GONE else View.VISIBLE
            fab.text = if (continueEpisodeNumber != null) {
                if (continueEpisodeNumber <= 1) {
                    safeContext.getString(R.string.start_episode, continueEpisodeNumber)
                } else {
                    safeContext.getString(R.string.continue_episode, continueEpisodeNumber)
                }
            } else {
                safeContext.getString(R.string.continue_string)
            }
            fab.setIconResource(R.drawable.ic_baseline_play_arrow_24)
            fab.setOnClickListener {
                (burningSeriesViewModel.continueSeriesEpisode ?: continueEpisode)?.let { episode -> episodeRecyclerClick(episode) }
            }
            binding.selectSeason.nextFocusRightId = fab.id
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.series_menu, menu)
        favIconColorApply(burningSeriesViewModel.seriesFavorite.value, menu.findItem(R.id.series_favorite))

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.series_favorite -> {
                val emit = burningSeriesViewModel.currentSeriesData?.apply {
                    series.favoriteSince = if (series.favoriteSince <= 0) Clock.System.now().epochSeconds else 0L
                }
                if (emit != null) {
                    burningSeriesViewModel.updateSeriesFavorite(emit)
                    favIconColorApply(emit.series.favoriteSince > 0, item)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun initActivityViews() {
        super.initActivityViews()

        exitFullScreen()
        hideNavigationFabs()
        showToolbarBackButton()
        setHasOptionsMenu(true)
        appBarLayout?.setExpandable(true)
        showSeriesArc()
        appBarLayout?.setExpanded(true, false)
        if (view != null) {
            applyContinueFab()
            binding.root.post {
                binding.root.fling(0)
            }
        }
    }
}