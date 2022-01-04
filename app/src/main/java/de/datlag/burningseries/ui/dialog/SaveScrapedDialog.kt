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
import de.datlag.burningseries.common.isTvOrLandscape
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.DialogSaveScrapedBinding
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class SaveScrapedDialog : BottomSheetDialogFragment() {

    private val binding: DialogSaveScrapedBinding by viewBinding()
    private val navArgs: SaveScrapedDialogArgs by navArgs()

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

        return inflater.inflate(R.layout.dialog_save_scraped, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (navArgs.saved) {
            setSuccess()
        } else {
            setFailed()
        }
        binding.continueButton.setOnClickListener {
            dismiss()
        }
        binding.backButton.setOnClickListener {
            findNavController().navigate(SaveScrapedDialogDirections.actionSaveScrapedDialogToSeriesFragment(seriesWithInfo = navArgs.seriesWithInfo))
        }
    }

    private fun setSuccess(): Unit = with(binding) {
        header.text = safeContext.getString(R.string.save_success)
        text.text = safeContext.getString(R.string.scraped_save_success)
    }

    private fun setFailed(): Unit = with(binding) {
        header.text = safeContext.getString(R.string.save_failed)
        text.text = safeContext.getString(R.string.scraped_save_failed)
    }
}