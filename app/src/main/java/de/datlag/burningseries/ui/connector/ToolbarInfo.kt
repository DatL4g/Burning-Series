package de.datlag.burningseries.ui.connector

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import io.github.florent37.shapeofview.shapes.ArcView
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface ToolbarInfo {
    val appbarLayout: AppBarLayout
    val collapsingToolbarLayout: CollapsingToolbarLayout
    val toolbar: MaterialToolbar
    val searchView: SimpleSearchView

    val seriesCover: ImageView
    val seriesArcWrapper: LinearLayoutCompat?
    val seriesArc: ArcView?
    val sizeHolder: View
}