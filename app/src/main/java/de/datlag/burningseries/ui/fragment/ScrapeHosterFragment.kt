package de.datlag.burningseries.ui.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dolatkia.animatedThemeManager.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.common.hideLoadingDialog
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.common.showLoadingDialog
import de.datlag.burningseries.databinding.FragmentScrapeHosterBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.ui.webview.AdBlockWebViewClient
import de.datlag.burningseries.viewmodel.AdBlockViewModel
import de.datlag.burningseries.viewmodel.ScrapeHosterViewModel
import de.datlag.model.Constants
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
@Obfuscate
class ScrapeHosterFragment : AdvancedFragment(R.layout.fragment_scrape_hoster) {

    private val navArgs: ScrapeHosterFragmentArgs by navArgs()
    private val binding: FragmentScrapeHosterBinding by viewBinding()
    private val viewModel: ScrapeHosterViewModel by activityViewModels()
    private val adBlockViewModel: AdBlockViewModel by activityViewModels()

    private val loadingStartedListener = {
        showLoadingDialog()
    }

    private val loadingFinishedListener = {
        hideLoadingDialog()
    }

    private val lazyErrorListener: (Uri?) -> Unit = {
        hideLoadingDialog()
        findNavController().navigate(ScrapeHosterFragmentDirections.actionScrapeHosterFragmentToWebViewErrorDialog(
            it?.toString() ?: binding.webView.url ?: navArgs.href,
            navArgs.seriesWithInfo
        ))
    }

    private val adBlockWebViewClient = AdBlockWebViewClient(setOf(Constants.HOST_BS_TO), loadingStartedListener, loadingFinishedListener, lazyErrorListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adBlockViewModel.loadAdBlockList(safeContext.resources.openRawResource(R.raw.adblock))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initWebView()
        listenAdBlockList()
        binding.webView.loadUrl(Constants.getBurningSeriesLink(navArgs.href))
        saveStream()
    }

    override fun syncTheme(appTheme: AppTheme) { }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(): Unit = with(binding) {
        webView.webViewClient = adBlockWebViewClient

        webView.settings.apply {
            allowFileAccess = false
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            mediaPlaybackRequiresUserGesture = true
        }
    }

    private fun listenAdBlockList() = adBlockViewModel.adBlockList.distinctUntilChanged().launchAndCollect {
        adBlockWebViewClient.adBlockList.emit(it)
    }

    private fun saveStream() = lifecycleScope.launch(Dispatchers.IO) {
        val scrapeJsInput = safeContext.resources.openRawResource(R.raw.scrape_hoster)
        val jsText = String(scrapeJsInput.readBytes())
        while (view != null) {
            if (view != null) {
                withContext(Dispatchers.Main) {
                    binding.webView.evaluateJavascript(jsText) {
                        if (it != null && it.isNotEmpty() && !it.equals("null", true)) {
                            saveScrapedData(it)
                        }
                    }
                }
            }
            delay(2000)
        }
    }

    private fun saveScrapedData(data: String) = viewModel.saveIfNotPresent(data).launchAndCollect {
        findNavController().navigate(ScrapeHosterFragmentDirections.actionScrapeHosterFragmentToSaveScrapedDialog(it, navArgs.seriesWithInfo))
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
    }
}