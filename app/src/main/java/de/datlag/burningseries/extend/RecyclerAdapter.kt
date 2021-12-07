package de.datlag.burningseries.extend

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
abstract class RecyclerAdapter<ReturnType, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
	
	abstract val diffCallback: DiffUtil.ItemCallback<ReturnType>
	abstract val differ: AsyncListDiffer<ReturnType>
	
	override fun getItemCount(): Int {
		return differ.currentList.size
	}
	
	fun submitList(list: Collection<ReturnType>) = differ.submitList(list.toList())
}
