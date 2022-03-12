package de.datlag.burningseries.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import de.datlag.burningseries.R
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.databinding.RecyclerDeviceDiscoveryBinding
import de.datlag.burningseries.extend.ClickRecyclerAdapter
import de.datlag.burningseries.model.HostOptionalInfo
import de.datlag.burningseries.module.NetworkModule
import de.datlag.coilifier.Scale
import de.datlag.coilifier.commons.load
import de.datlag.k2k.Host
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject

@Obfuscate
class DeviceDiscoveryAdapter : ClickRecyclerAdapter<Host, DeviceDiscoveryAdapter.ViewHolder>() {

    override val diffCallback = object : DiffUtil.ItemCallback<Host>() {
        override fun areItemsTheSame(oldItem: Host, newItem: Host): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Host, newItem: Host): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    override val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val binding: RecyclerDeviceDiscoveryBinding by viewBinding(RecyclerDeviceDiscoveryBinding::bind)

        init {
            binding.icon.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(differ.currentList[absoluteAdapterPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateView(R.layout.recycler_device_discovery))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
        val item = differ.currentList[position]

        val isTv = item.optionalInfo?.let { NetworkModule.jsonBuilder.decodeFromJsonElement<HostOptionalInfo>(it) }?.isTv ?: false
        binding.icon.load<Drawable>(if (isTv) R.drawable.ic_baseline_tv_24 else R.drawable.ic_baseline_phone_android_24) {
            scaleType(Scale.CENTER_INSIDE)
        }
        binding.title.text = item.name
    }
}