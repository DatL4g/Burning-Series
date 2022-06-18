package de.datlag.burningseries.extend

import android.view.View
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
abstract class RecyclerAdapter<ReturnType, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
	
	abstract val diffCallback: DiffUtil.ItemCallback<ReturnType>
	abstract val differ: AsyncListDiffer<ReturnType>

	protected var focusChangeListener: View.OnFocusChangeListener? = null
	
	override fun getItemCount(): Int {
		return differ.currentList.size
	}
	
	fun submitList(list: Collection<ReturnType>) = differ.submitList(list.toList())
	fun submitList(list: Collection<ReturnType>, callback: () -> Unit) = differ.submitList(list.toList(), callback)

	fun resubmitList() = submitList(differ.currentList)

	fun setOnFocusChangeListener(listener: View.OnFocusChangeListener) {
		focusChangeListener = listener
	}

	fun setOnFocusChangeListener(listener: (view: View, hasFocus: Boolean) -> Unit) = setOnFocusChangeListener(
		View.OnFocusChangeListener { v, hasFocus -> listener.invoke(v, hasFocus) }
	)
}
