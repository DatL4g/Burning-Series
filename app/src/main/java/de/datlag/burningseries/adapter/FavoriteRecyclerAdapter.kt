package de.datlag.burningseries.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import de.datlag.burningseries.R
import de.datlag.burningseries.common.anyHeight
import de.datlag.burningseries.common.anyWidth
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerFavoriteBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.coilifier.BlurHash
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import io.michaelrocks.paranoid.Obfuscate
import java.io.File

@Obfuscate
class FavoriteRecyclerAdapter(
    private val coversDir: File,
    private val blurHash: BlurHash
) : ClickRecyclerAdapter<SeriesWithInfo, FavoriteRecyclerAdapter.ViewHolder>() {

    override val diffCallback = object : DiffUtil.ItemCallback<SeriesWithInfo>() {
        override fun areItemsTheSame(oldItem: SeriesWithInfo, newItem: SeriesWithInfo): Boolean {
            return oldItem.series.seriesId == newItem.series.seriesId
        }

        override fun areContentsTheSame(oldItem: SeriesWithInfo, newItem: SeriesWithInfo): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    override val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val binding: RecyclerFavoriteBinding by viewBinding(RecyclerFavoriteBinding::bind)

        init {
            binding.card.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(differ.currentList[absoluteAdapterPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateView(R.layout.recycler_favorite))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
        val item = differ.currentList[position]

        val cover = item.cover ?: item.series.cover
        val errorImage = cover.loadBase64Image(coversDir)

        binding.cover.load<Drawable>(Constants.getBurningSeriesLink(cover.href)) {
            val width = binding.cover.anyWidth ?: 0
            val height = binding.cover.anyHeight ?: 0

            if (errorImage != null) {
                error(errorImage)
            } else if (cover.blurHash.isNotEmpty() && width > 0 && height > 0) {
                error(cover.loadBlurHash {
                    blurHash.execute(cover.blurHash, width, height)
                })
            }
        }
        binding.title.text = item.series.title
    }
}