package de.datlag.burningseries.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.burningseries.BuildConfig
import de.datlag.burningseries.R
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.DialogNewReleaseBinding
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class NewReleaseDialog : BottomSheetDialogFragment() {

    private val binding: DialogNewReleaseBinding by viewBinding(DialogNewReleaseBinding::bind)
    private val navArgs: NewReleaseDialogArgs by navArgs()

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

        return inflater.inflate(R.layout.dialog_new_release, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.text.text = safeContext.getString(
            R.string.new_release_text,
            navArgs.release.tagName,
            BuildConfig.VERSION_NAME,
            safeContext.getString(if (navArgs.release.isPreRelease) R.string.yes else R.string.no)
        )
        binding.viewButton.setOnClickListener {
            dismiss()
            navArgs.release.htmlUrl.toUri().openInBrowser(safeContext, safeContext.getString(R.string.new_release))
        }
        binding.backButton.setOnClickListener {
            dismiss()
        }
    }
}