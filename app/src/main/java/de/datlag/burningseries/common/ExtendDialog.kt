package de.datlag.burningseries.common

import android.content.DialogInterface
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

fun DialogInterface.expand() {
    if (this is BottomSheetDialog) {
        val bottomSheet = this
        val sheetInternal: View? = bottomSheet.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        sheetInternal?.let { sheet ->
            sheet.post {
                BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }
}