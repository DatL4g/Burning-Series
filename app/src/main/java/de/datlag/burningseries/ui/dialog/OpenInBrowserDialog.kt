package de.datlag.burningseries.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.burningseries.R
import de.datlag.burningseries.common.expand
import de.datlag.burningseries.common.isTelevision
import de.datlag.burningseries.common.openInBrowser
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.DialogOpenInBrowserBinding

class OpenInBrowserDialog : BottomSheetDialogFragment() {

    private val binding: DialogOpenInBrowserBinding by viewBinding()
    private val navArgs: OpenInBrowserDialogArgs by navArgs()

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
            navArgs.href.toUri().openInBrowser(safeContext)
        }
    }
}