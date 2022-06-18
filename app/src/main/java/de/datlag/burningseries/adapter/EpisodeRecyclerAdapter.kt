package de.datlag.burningseries.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import de.datlag.burningseries.R
import de.datlag.burningseries.common.gone
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.common.visible
import de.datlag.burningseries.databinding.RecyclerEpisodeBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.coilifier.commons.load
import de.datlag.model.burningseries.series.relation.EpisodeWithHoster
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class EpisodeRecyclerAdapter(
    private val rightFocusViewId: Int? = null
) : ClickRecyclerAdapter<EpisodeWithHoster, EpisodeRecyclerAdapter.ViewHolder>() {

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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener,
        View.OnLongClickListener,
        View.OnFocusChangeListener {

        val binding: RecyclerEpisodeBinding by viewBinding(RecyclerEpisodeBinding::bind)

        init {
            binding.card.setOnClickListener(this)
            binding.card.setOnLongClickListener(this)
            binding.card.onFocusChangeListener = this
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(differ.currentList[absoluteAdapterPosition])
        }

        override fun onLongClick(v: View?): Boolean {
            return longClickListener?.invoke(differ.currentList[absoluteAdapterPosition]) ?: false
        }

        override fun onFocusChange(v: View?, hasFocus: Boolean) {
            focusChangeListener?.onFocusChange(v, hasFocus)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateView(R.layout.recycler_episode))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
        val item = differ.currentList[position]

        if (rightFocusViewId != null) {
            binding.card.nextFocusRightId = rightFocusViewId
        }
        binding.number.text = item.episode.number
        binding.title.text = item.episode.title
        val watchedProgress = item.episode.watchedPercentage()
        when {
            watchedProgress == 0F -> {
                binding.progressIcon.load<Drawable>(null)
                binding.progressIcon.gone()
            }
            watchedProgress >= 85F -> {
                binding.progressIcon.load<Drawable>(R.drawable.ic_baseline_check_24)
                binding.progressIcon.visible()
            }
            watchedProgress > 0F -> {
                binding.progressIcon.load<Drawable>(R.drawable.ic_baseline_play_arrow_24)
                binding.progressIcon.visible()
            }
        }
        if (item.episode.hoster.isEmpty() && item.hoster.isEmpty()) {
            binding.card.isEnabled = false
            binding.card.alpha = 0.5F
        } else {
            binding.card.isEnabled = true
            binding.card.alpha = 1F
        }
    }

}