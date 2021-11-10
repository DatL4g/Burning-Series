package de.datlag.burningseries.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import de.datlag.burningseries.R
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerLatestEpisodeBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.model.burningseries.home.LatestEpisode
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class LatestEpisodeRecyclerAdapter : ClickRecyclerAdapter<LatestEpisode, LatestEpisodeRecyclerAdapter.ViewHolder>() {
	
	override val diffCallback = object : DiffUtil.ItemCallback<LatestEpisode>() {
		override fun areItemsTheSame(oldItem: LatestEpisode, newItem: LatestEpisode): Boolean {
			return oldItem.href == newItem.href
		}
		
		override fun areContentsTheSame(oldItem: LatestEpisode, newItem: LatestEpisode): Boolean {
			return oldItem.hashCode() == newItem.hashCode()
		}
	}
	
	override val differ = AsyncListDiffer(this, diffCallback)
	
	inner class ViewHolder(
		itemView: View
	) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
		val binding: RecyclerLatestEpisodeBinding by viewBinding()
		
		init {
			binding.card.setOnClickListener(this)
			binding.card.setOnLongClickListener(this)
		}
		
		override fun onClick(p0: View?) {
			clickListener?.invoke(p0 ?: itemView, differ.currentList[absoluteAdapterPosition])
		}
		
		override fun onLongClick(v: View?): Boolean {
			return longClickListener?.invoke(v ?: itemView, differ.currentList[absoluteAdapterPosition]) ?: false
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(parent.inflateView(R.layout.recycler_latest_episode))
	}
	
	override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
		val item = differ.currentList[position]
		val (title, text) = item.getEpisodeAndSeries()
		
		binding.title.text = title
		binding.text.text = text
	}
}