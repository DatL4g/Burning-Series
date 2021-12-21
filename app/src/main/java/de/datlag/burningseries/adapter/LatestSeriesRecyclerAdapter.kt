package de.datlag.burningseries.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import de.datlag.burningseries.R
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerLatestSeriesBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.model.burningseries.home.LatestSeries
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class LatestSeriesRecyclerAdapter(
	private val aboveFocusViewId: Int? = null,
	private val belowFocusViewId: Int? = null
) : ClickRecyclerAdapter<LatestSeries, LatestSeriesRecyclerAdapter.ViewHolder>() {
	
	override val diffCallback = object: DiffUtil.ItemCallback<LatestSeries>() {
		override fun areItemsTheSame(oldItem: LatestSeries, newItem: LatestSeries): Boolean {
			return oldItem.href == newItem.href
		}
		
		override fun areContentsTheSame(oldItem: LatestSeries, newItem: LatestSeries): Boolean {
			return oldItem.hashCode() == newItem.hashCode()
		}
	}
	
	override val differ = AsyncListDiffer(this, diffCallback)
	
	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
		val binding: RecyclerLatestSeriesBinding by viewBinding()
		
		init {
			binding.card.setOnClickListener(this)
			binding.card.setOnLongClickListener(this)
		}
		
		override fun onClick(p0: View?) {
			clickListener?.invoke(differ.currentList[absoluteAdapterPosition])
		}

		override fun onLongClick(v: View?): Boolean {
			return longClickListener?.invoke(differ.currentList[absoluteAdapterPosition]) ?: false
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(parent.inflateView(R.layout.recycler_latest_series))
	}
	
	override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
		val item = differ.currentList[position]
		
		binding.title.text = item.title

		if (position == 0 && aboveFocusViewId != null) {
			binding.card.nextFocusUpId = aboveFocusViewId
		}

		if (position == differ.currentList.size - 1 && belowFocusViewId != null) {
			binding.card.nextFocusDownId = belowFocusViewId
		}
	}
}