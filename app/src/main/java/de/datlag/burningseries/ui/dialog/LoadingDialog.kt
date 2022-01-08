package de.datlag.burningseries.ui.dialog

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import de.datlag.burningseries.R
import de.datlag.burningseries.common.expand
import de.datlag.burningseries.common.isTelevision
import io.michaelrocks.paranoid.Obfuscate
import java.util.concurrent.atomic.AtomicReference

@Obfuscate
class LoadingDialog private constructor(context: Context) : BottomSheetDialog(context, R.style.BottomSheetDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (context.packageManager.isTelevision() || context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setOnShowListener {
                it.expand()
            }
        }

        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setOnCancelListener {
            currentInstance.compareAndSet(this, null)
        }
        setOnDismissListener {
            cancel()
        }

        setContentView(R.layout.dialog_loading)
    }

    companion object {
        private val currentInstance: AtomicReference<LoadingDialog?> = AtomicReference(null)

        private fun getInstance(context: Context): LoadingDialog {
            return nullableInstance ?: run {
                val newDialog = LoadingDialog(context)
                while (!currentInstance.compareAndSet(null, newDialog)) {
                    dismiss()
                }
                nullableInstance ?: newDialog
            }
        }

        private val nullableInstance
            get() = currentInstance.get()

        fun show(context: Context) {
            val instance = getInstance(context)
            if (!instance.isShowing) {
                instance.show()
            }
        }

        fun dismiss() {
            nullableInstance?.dismiss()
            nullableInstance?.cancel()
        }
    }
}