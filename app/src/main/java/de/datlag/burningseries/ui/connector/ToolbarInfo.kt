package de.datlag.burningseries.ui.connector

import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import io.github.florent37.shapeofview.shapes.ArcView
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface ToolbarInfo {
    val seriesCover: ImageView
    val seriesArcWrapper: LinearLayoutCompat?
    val seriesArc: ArcView?
}