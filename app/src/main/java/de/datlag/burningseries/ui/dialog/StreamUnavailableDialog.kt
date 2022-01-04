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
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.DialogStreamUnavailableBinding
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class StreamUnavailableDialog : BottomSheetDialogFragment() {

    private val binding: DialogStreamUnavailableBinding by viewBinding()
    private val navArgs: StreamUnavailableDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isTvOrLandscape()) {
            dialog?.setOnShowListener {
                it.expand()
            }
        }

        return inflater.inflate(R.layout.dialog_stream_unavailable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.hosterButton.setOnClickListener {
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
            findNavController().navigate(StreamUnavailableDialogDirections.actionStreamUnavailableDialogToSeriesFragment(seriesWithInfo = navArgs.seriesWithInfo))
        }
    }
}