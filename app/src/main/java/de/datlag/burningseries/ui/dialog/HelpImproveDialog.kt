package de.datlag.burningseries.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.burningseries.R
import de.datlag.burningseries.common.expand
import de.datlag.burningseries.common.isTelevision
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.DialogHelpImproveBinding
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class HelpImproveDialog : BottomSheetDialogFragment() {

    private val binding: DialogHelpImproveBinding by viewBinding()
    private val navArgs: HelpImproveDialogArgs by navArgs()

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

        return inflater.inflate(R.layout.dialog_help_improve, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.number.text = navArgs.count.toString()
        binding.closeButton.setOnClickListener { this.dismiss() }
    }
}