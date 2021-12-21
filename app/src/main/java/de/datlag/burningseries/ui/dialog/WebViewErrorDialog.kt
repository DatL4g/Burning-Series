package de.datlag.burningseries.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.burningseries.R
import de.datlag.burningseries.common.expand
import de.datlag.burningseries.common.isTelevision
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.DialogWebviewErrorBinding

class WebViewErrorDialog : BottomSheetDialogFragment() {

    private val binding: DialogWebviewErrorBinding by viewBinding()
    private val navArgs: WebViewErrorDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (safeContext.packageManager.isTelevision()) {
            dialog?.setOnShowListener {
                it.expand()
            }
        }

        return inflater.inflate(R.layout.dialog_webview_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.text.text = safeContext.getString(R.string.error_loading_text, navArgs.href)
        binding.backButton.setOnClickListener {
            dismiss()
            findNavController().navigate(WebViewErrorDialogDirections.actionWebViewErrorDialogToSeriesFragment(
                seriesWithInfo = navArgs.seriesWithInfo
            ))
        }
        binding.retryButton.setOnClickListener {
            dismiss()
            findNavController().navigate(WebViewErrorDialogDirections.actionWebViewErrorDialogToScrapeHosterFragment(
                navArgs.href,
                navArgs.seriesWithInfo
            ))
        }
    }
}