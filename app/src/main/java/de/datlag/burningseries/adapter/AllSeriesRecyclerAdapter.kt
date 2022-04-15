package de.datlag.burningseries.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dolatkia.animatedThemeManager.ThemeManager
import de.datlag.burningseries.R
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerAllSeriesHeaderBinding
import de.datlag.burningseries.databinding.RecyclerAllSeriesItemBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.model.burningseries.allseries.GenreModel
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class AllSeriesRecyclerAdapter(
    private val rightFocusViewId: Int? = null,
    private val belowFocusViewId: Int? = null
) : ClickRecyclerAdapter<GenreModel, AllSeriesRecyclerAdapter.ViewHolder>() {

    override val diffCallback = object : DiffUtil.ItemCallback<GenreModel>() {
        override fun areItemsTheSame(oldItem: GenreModel, newItem: GenreModel): Boolean {
            return if (oldItem is GenreModel.GenreData && newItem is GenreModel.GenreData) {
                oldItem.genreId == newItem.genreId
            } else if (oldItem is GenreModel.GenreItem && newItem is GenreModel.GenreItem) {
                oldItem.genreItemId == newItem.genreItemId || oldItem.href == newItem.href
            } else {
                false
            }
        }

        override fun areContentsTheSame(oldItem: GenreModel, newItem: GenreModel): Boolean {
            return if (oldItem is GenreModel.GenreData && newItem is GenreModel.GenreData) {
                oldItem.hashCode() == newItem.hashCode()
            } else if (oldItem is GenreModel.GenreItem && newItem is GenreModel.GenreItem) {
                oldItem.hashCode() == newItem.hashCode()
            } else {
                oldItem.hashCode() == newItem.hashCode()
            }
        }
    }

    override val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private fun bindHeader(item: GenreModel.GenreData) {
            val binding = RecyclerAllSeriesHeaderBinding.bind(itemView)

            val appTheme = ThemeManager.currentTheme as? ApplicationTheme?
            appTheme?.let {
                binding.card.setBackgroundColor(it.defaultBackgroundColor(binding.card.context))
                binding.title.setTextColor(it.defaultContentColor(binding.title.context))
            }

            binding.title.text = item.genre
        }

        private fun bindItem(item: GenreModel.GenreItem, position: Int) {
            val binding = RecyclerAllSeriesItemBinding.bind(itemView)
            binding.card.setOnClickListener(this)
            binding.card.setOnLongClickListener(this)

            val appTheme = ThemeManager.currentTheme as? ApplicationTheme?
            appTheme?.let {
                binding.card.setCardBackgroundColor(it.defaultBackgroundColor(binding.card.context))
                binding.title.setTextColor(it.defaultContentColor(binding.title.context))
                binding.title.setBackgroundColor(it.defaultBackgroundColor(binding.title.context))
            }

            binding.title.text = item.title

            if (rightFocusViewId != null) {
                binding.card.nextFocusRightId = rightFocusViewId
            }

            if (position == differ.currentList.size - 1 && belowFocusViewId != null) {
                binding.card.nextFocusDownId = belowFocusViewId
            }
        }

        fun bind(genreModel: GenreModel, position: Int) {
            when (genreModel) {
                is GenreModel.GenreData -> bindHeader(genreModel)
                is GenreModel.GenreItem -> bindItem(genreModel, position)
            }
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(differ.currentList[absoluteAdapterPosition])
        }

        override fun onLongClick(v: View?): Boolean {
            return longClickListener?.invoke(differ.currentList[absoluteAdapterPosition]) ?: false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            TYPE_HEADER -> R.layout.recycler_all_series_header
            TYPE_ITEM -> R.layout.recycler_all_series_item
            else -> throw IllegalArgumentException("Invalid type: $viewType")
        }
        return ViewHolder(parent.inflateView(layout))
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is GenreModel.GenreData -> TYPE_HEADER
            is GenreModel.GenreItem -> TYPE_ITEM
            else -> super.getItemViewType(position)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
        bind(differ.currentList[position], position)
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }
}