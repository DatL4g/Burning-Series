package de.datlag.burningseries.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dolatkia.animatedThemeManager.ThemeManager
import de.datlag.burningseries.R
import de.datlag.burningseries.common.clearTint
import de.datlag.burningseries.common.hide
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.common.show
import de.datlag.burningseries.databinding.RecyclerEpisodeBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.coilifier.commons.load
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val binding: RecyclerEpisodeBinding by viewBinding(RecyclerEpisodeBinding::bind)

        init {
            binding.card.setOnClickListener(this)
            binding.card.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(differ.currentList[absoluteAdapterPosition])
        }

        override fun onLongClick(v: View?): Boolean {
            return longClickListener?.invoke(differ.currentList[absoluteAdapterPosition]) ?: false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateView(R.layout.recycler_episode))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
        val item = differ.currentList[position]

        val appTheme = ThemeManager.currentTheme as? ApplicationTheme?
        appTheme?.let {
            binding.card.setBackgroundColor(it.defaultBackgroundColor(binding.card.context))
            binding.number.setTextColor(it.defaultContentColor(binding.number.context))
            binding.title.setTextColor(it.defaultContentColor(binding.title.context))
            binding.progressIcon.clearTint()
            binding.progressIcon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(it.defaultContentColor(binding.progressIcon.context), BlendModeCompat.SRC_IN)
        }

        binding.number.text = item.episode.number
        binding.title.text = item.episode.title
        val watchedProgress = item.episode.watchedPercentage()
        when {
            watchedProgress == 0F -> {
                binding.progressIcon.load<Drawable>(null)
                binding.progressIcon.hide()
            }
            watchedProgress >= 90F -> {
                binding.progressIcon.load<Drawable>(R.drawable.ic_baseline_check_24)
                binding.progressIcon.show()
            }
            watchedProgress > 0F -> {
                binding.progressIcon.load<Drawable>(R.drawable.ic_baseline_play_arrow_24)
                binding.progressIcon.show()
            }
        }
    }

}