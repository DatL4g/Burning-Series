package de.datlag.burningseries.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import de.datlag.burningseries.R
import de.datlag.burningseries.common.context
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerEpisodeBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.model.burningseries.series.relation.EpisodeWithHoster
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class EpisodeRecyclerAdapter : ClickRecyclerAdapter<EpisodeWithHoster, EpisodeRecyclerAdapter.ViewHolder>() {

    override val diffCallback = object : DiffUtil.ItemCallback<EpisodeWithHoster>() {
        override fun areItemsTheSame(
            oldItem: EpisodeWithHoster,
            newItem: EpisodeWithHoster
        ): Boolean {
            return oldItem.episode.episodeId == newItem.episode.episodeId
        }

        override fun areContentsTheSame(
            oldItem: EpisodeWithHoster,
            newItem: EpisodeWithHoster
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    override val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val binding: RecyclerEpisodeBinding by viewBinding()

        init {
            binding.card.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(v ?: itemView, differ.currentList[absoluteAdapterPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateView(R.layout.recycler_episode))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
        val item = differ.currentList[position]

        binding.number.text = item.episode.number
        binding.title.text = item.episode.title
    }
}