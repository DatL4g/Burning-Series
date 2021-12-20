package de.datlag.burningseries.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.burningseries.R
import de.datlag.burningseries.common.expand
import de.datlag.burningseries.common.isTelevision
import de.datlag.burningseries.common.openInBrowser
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.DialogStreamUnavailableBinding

class StreamUnavailableDialog : BottomSheetDialogFragment() {

    private val binding: DialogStreamUnavailableBinding by viewBinding()
    private val navArgs: StreamUnavailableDialogArgs by navArgs()

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

        return inflater.inflate(R.layout.dialog_stream_unavailable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.hosterButton.setOnClickListener {
            dismiss()
            findNavController().navigate(StreamUnavailableDialogDirections.actionStreamUnavailableDialogToScrapeHosterFragment(
                navArgs.bsUrl,
                navArgs.seriesWithInfo
            ))
        }
        binding.browserButton.setOnClickListener {
            dismiss()
            navArgs.href.toUri().openInBrowser(safeContext)
        }
        binding.backButton.setOnClickListener {
            dismiss()
            findNavController().navigate(StreamUnavailableDialogDirections.actionStreamUnavailableDialogToSeriesFragment(seriesWithInfo = navArgs.seriesWithInfo))
        }
    }
}