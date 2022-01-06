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
import de.datlag.burningseries.databinding.DialogOpenInBrowserBinding
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class OpenInBrowserDialog : BottomSheetDialogFragment() {

    private val binding: DialogOpenInBrowserBinding by viewBinding(DialogOpenInBrowserBinding::bind)
    private val navArgs: OpenInBrowserDialogArgs by navArgs()

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

        return inflater.inflate(R.layout.dialog_open_in_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.text.text = safeContext.getString(R.string.open_in_browser_text, navArgs.title ?: navArgs.href)
        binding.backButton.setOnClickListener {
            dismiss()
        }
        binding.openButton.setOnClickListener {
            dismiss()
            navArgs.href.toUri().openInBrowser(safeContext, navArgs.title)
        }

        if (navArgs.seriesWithInfo != null) {
            binding.hosterButton.show()
            binding.hosterButton.setOnClickListener {
                findNavController().navigate(OpenInBrowserDialogDirections.actionOpenInBrowserDialogToScrapeHosterFragment(
                    navArgs.href,
                    navArgs.seriesWithInfo!!
                ))
            }
        } else {
            binding.hosterButton.hide()
        }
    }
}