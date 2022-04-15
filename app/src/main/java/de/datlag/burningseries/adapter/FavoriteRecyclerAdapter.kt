package de.datlag.burningseries.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dolatkia.animatedThemeManager.ThemeManager
import de.datlag.burningseries.R
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerFavoriteBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.coilifier.commons.load
import de.datlag.model.Constants
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class FavoriteRecyclerAdapter(private val fragment: AdvancedFragment) : ClickRecyclerAdapter<SeriesWithInfo, FavoriteRecyclerAdapter.ViewHolder>() {

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

        val appTheme = ThemeManager.currentTheme as? ApplicationTheme?
        appTheme?.let {
            binding.parent.setBackgroundColor(it.defaultBackgroundColor(binding.parent.context))
            binding.card.setCardBackgroundColor(it.defaultBackgroundColor(binding.card.context))
            binding.cover.setBackgroundColor(it.defaultBackgroundColor(binding.cover.context))
            binding.title.setTextColor(it.defaultContentColor(binding.title.context))
        }

        fragment.loadImageAndSave(Constants.getBurningSeriesLink(item.series.image)) {
            binding.cover.load<Drawable>(it)
        }
        binding.title.text = item.series.title
    }
}