package de.datlag.burningseries.adapter

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
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerLatestEpisodeBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.model.burningseries.home.LatestEpisode
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class LatestEpisodeRecyclerAdapter(private val belowFocusViewId: Int) : ClickRecyclerAdapter<LatestEpisode, LatestEpisodeRecyclerAdapter.ViewHolder>() {

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

		val appTheme = ThemeManager.instance.getCurrentTheme() as? ApplicationTheme?
		appTheme?.let {
			binding.card.setCardBackgroundColor(it.defaultBackgroundColor(binding.card.context))
			binding.title.setTextColor(it.defaultContentColor(binding.title.context))
			binding.title.setBackgroundColor(it.defaultBackgroundColor(binding.title.context))
			binding.text.setTextColor(it.defaultContentColor(binding.text.context))
			binding.text.setBackgroundColor(it.defaultBackgroundColor(binding.text.context))
			binding.icon.clearTint()
			binding.icon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(it.defaultContentColor(binding.icon.context), BlendModeCompat.SRC_IN)
		}

		binding.title.text = title
		binding.text.text = text
		if (position == differ.currentList.size - 1) {
			binding.card.nextFocusDownId = belowFocusViewId
		}
	}
}