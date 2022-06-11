package de.datlag.burningseries.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import de.datlag.burningseries.R
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerSeriesInfoBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.model.burningseries.series.InfoData
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class SeriesInfoAdapter : ClickRecyclerAdapter<InfoData, SeriesInfoAdapter.ViewHolder>() {

    override val diffCallback = object : DiffUtil.ItemCallback<InfoData>() {
        override fun areItemsTheSame(
            oldItem: InfoData,
            newItem: InfoData
        ): Boolean {
            return oldItem.infoId == newItem.infoId
        }

        override fun areContentsTheSame(
            oldItem: InfoData,
            newItem: InfoData
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    override val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RecyclerSeriesInfoBinding by viewBinding(RecyclerSeriesInfoBinding::bind)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateView(R.layout.recycler_series_info))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
        val item = differ.currentList[position]

        binding.header.text = item.header.trim()
        binding.info.text = item.data.trim()
    }
}