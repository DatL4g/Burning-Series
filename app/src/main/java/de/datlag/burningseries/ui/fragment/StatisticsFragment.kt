package de.datlag.burningseries.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.db.williamchart.view.DonutChartView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.common.getColorCompat
import de.datlag.burningseries.common.gone
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.FragmentStatisticsBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.viewmodel.SettingsViewModel
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
@Obfuscate
class StatisticsFragment : AdvancedFragment(R.layout.fragment_statistics) {

    private val binding: FragmentStatisticsBinding by viewBinding()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).also { view ->
            view?.findViewById<DonutChartView>(R.id.pointsChart)?.show(listOf())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pointsChart.donutColors = intArrayOf(safeContext.getColorCompat(R.color.onBackgroundColor), safeContext.getColorCompat(R.color.onBackgroundColor))
        loadUsage()
    }

    private fun loadUsage() = settingsViewModel.data.map { it.usage }.launchAndCollect {
        binding.pointsChart.animate(listOf(it.saveAmount.toFloat(), it.timeEditAmount))
        binding.pointsText.text = safeContext.getString(R.string.points_out_of_500, (it.saveAmount.toFloat() + it.timeEditAmount))
    }

    override fun initActivityViews() {
        super.initActivityViews()

        exitFullScreen()
        hideSeriesArc()
        showToolbarBackButton()
        setToolbarTitle(R.string.statistics)
        appBarLayout?.setExpanded(false, false)
        appBarLayout?.setExpandable(false)
        hideNavigationFabs()
        extendedFab?.gone()
    }
}