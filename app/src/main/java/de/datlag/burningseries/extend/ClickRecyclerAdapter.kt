package de.datlag.burningseries.extend

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

	fun performClickOn(predicate: (ReturnType) -> Boolean) {
		differ.currentList.firstOrNull { predicate.invoke(it) }?.let {
			clickListener?.invoke(it)
		}
	}
}

typealias recyclerClickListener<ReturnType> = (item: ReturnType) -> Unit
typealias recyclerLongClickListener<ReturnType> = (item: ReturnType) -> Boolean