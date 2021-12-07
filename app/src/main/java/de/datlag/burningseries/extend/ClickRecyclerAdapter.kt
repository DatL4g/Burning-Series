package de.datlag.burningseries.extend

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
abstract class ClickRecyclerAdapter<ReturnType, VH : RecyclerView.ViewHolder> : RecyclerAdapter<ReturnType, VH>() {
	
	protected var clickListener: (recyclerClickListener<ReturnType>)? = null
	protected var longClickListener: (recyclerLongClickListener<ReturnType>)? = null
	
	
	fun setOnClickListener(listener: recyclerClickListener<ReturnType>) {
		clickListener = listener
	}
	
	fun setOnLongClickListener(listener: recyclerLongClickListener<ReturnType>) {
		longClickListener = listener
	}
}

typealias recyclerClickListener<ReturnType> = (view: View, item: ReturnType) -> Unit
typealias recyclerLongClickListener<ReturnType> = (view: View, item: ReturnType) -> Boolean