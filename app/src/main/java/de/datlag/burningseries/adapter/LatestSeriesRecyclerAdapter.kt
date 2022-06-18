package de.datlag.burningseries.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import de.datlag.burningseries.R
import de.datlag.burningseries.common.anyHeight
import de.datlag.burningseries.common.anyWidth
import de.datlag.burningseries.common.dpToPx
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerLatestSeriesBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.coilifier.BlurHash
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.burningseries.home.LatestSeries
import io.michaelrocks.paranoid.Obfuscate
import java.io.File

@Obfuscate
class LatestSeriesRecyclerAdapter(
	private val coversDir: File,
	private val blurHash: BlurHash
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
		val binding: RecyclerLatestSeriesBinding by viewBinding(RecyclerLatestSeriesBinding::bind)

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

		val errorImage = item.cover.loadBase64Image(coversDir)
		binding.cover.load<Drawable>(Constants.getBurningSeriesLink(item.cover.href)) {
			val width = binding.cover.anyWidth ?: 0
			val height = binding.cover.anyHeight ?: (width.toFloat() * 1.6F).toInt()

			if (errorImage != null) {
				error(errorImage)
			} else if (item.cover.blurHash.isNotEmpty() && width > 0 && height > 0) {
				error(item.cover.loadBlurHash {
					blurHash.execute(item.cover.blurHash, width, height)
				})
			}
		}
		binding.title.text = item.title
	}
}