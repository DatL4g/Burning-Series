package de.datlag.burningseries.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updateLayoutParams
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
import de.datlag.burningseries.databinding.RecyclerLatestEpisodeBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.coilifier.BlurHash
import de.datlag.coilifier.Scale
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.burningseries.home.LatestEpisode
import io.michaelrocks.paranoid.Obfuscate
import java.io.File

@Obfuscate
class LatestEpisodeRecyclerAdapter(
	private val coversDir: File,
	private val blurHash: BlurHash
) : ClickRecyclerAdapter<LatestEpisode, LatestEpisodeRecyclerAdapter.ViewHolder>() {

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
		val binding: RecyclerLatestEpisodeBinding by viewBinding(RecyclerLatestEpisodeBinding::bind)

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
		return ViewHolder(parent.inflateView(R.layout.recycler_latest_episode))
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
		val item = differ.currentList[position]
		val (title, text) = item.getEpisodeAndSeries()

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
		binding.title.text = title
		binding.text.text = text
		binding.flag.load<Drawable>(if (item.isJapanese) {
			R.drawable.ic_japan
		} else if (item.isGerman) {
			R.drawable.ic_germany
		} else if (item.isEnglish) {
			R.drawable.ic_usa
		} else if (item.isGermanSub) {
			R.drawable.ic_des
		} else if (item.isJapaneseSub) {
			R.drawable.ic_jps
		} else {
			null
		}) {
			transform(FitCenter(), RoundedCorners(binding.flag.context.dpToPx(4).toInt()))
		}
	}
}