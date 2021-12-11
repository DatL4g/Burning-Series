package de.datlag.burningseries.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.datlag.burningseries.R
import de.datlag.burningseries.common.hide
import de.datlag.burningseries.common.inflateView
import de.datlag.burningseries.common.show
import de.datlag.burningseries.databinding.RecyclerSettingsGroupBinding
import de.datlag.burningseries.databinding.RecyclerSettingsSwitchBinding
import de.datlag.burningseries.extend.RecyclerAdapter
import de.datlag.burningseries.model.SettingsModel
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class SettingsRecyclerAdapter : RecyclerAdapter<SettingsModel, SettingsRecyclerAdapter.ViewHolder>() {

    override val diffCallback = object : DiffUtil.ItemCallback<SettingsModel>() {
        override fun areItemsTheSame(oldItem: SettingsModel, newItem: SettingsModel): Boolean {
            return if (oldItem is SettingsModel.Group && newItem is SettingsModel.Group) {
                oldItem.title == newItem.title
            } else if (oldItem is SettingsModel.Switch && newItem is SettingsModel.Switch) {
                oldItem.title == newItem.title
            } else {
                false
            }
        }

        override fun areContentsTheSame(oldItem: SettingsModel, newItem: SettingsModel): Boolean {
            return if (oldItem is SettingsModel.Group && newItem is SettingsModel.Group) {
                oldItem.hashCode() == newItem.hashCode()
            } else if (oldItem is SettingsModel.Switch && newItem is SettingsModel.Switch) {
                oldItem.hashCode() == newItem.hashCode()
            } else {
                false
            }
        }
    }

    override val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private fun bindGroup(item: SettingsModel.Group, position: Int) {
            val binding = RecyclerSettingsGroupBinding.bind(itemView)
            binding.title.text = item.title
        }

        private fun bindSwitch(item: SettingsModel.Switch, position: Int) {
            val binding = RecyclerSettingsSwitchBinding.bind(itemView)
            binding.title.text = item.title
            binding.text.text = item.text
            if (item.text.isEmpty()) {
                binding.text.hide()
            } else {
                binding.text.show()
            }
            binding.switcher.isChecked = item.defaultValue
            binding.switcher.setOnCheckedChangeListener { _, isChecked ->
                item.listener.invoke(isChecked)
                submitList(differ.currentList.toMutableList().apply {
                    removeAt(position)
                    add(position, item.apply { defaultValue = isChecked })
                })
            }
        }

        fun bind(settingsModel: SettingsModel, position: Int) {
            when (settingsModel) {
                is SettingsModel.Group -> bindGroup(settingsModel, position)
                is SettingsModel.Switch -> bindSwitch(settingsModel, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is SettingsModel.Group -> TYPE_GROUP
            is SettingsModel.Switch -> TYPE_SWITCH
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            TYPE_GROUP -> R.layout.recycler_settings_group
            TYPE_SWITCH -> R.layout.recycler_settings_switch
            else -> throw IllegalArgumentException("Invalid type: $viewType")
        }
        return ViewHolder(parent.inflateView(layout))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder) {
        bind(differ.currentList[position], position)
    }

    companion object {
        private const val TYPE_GROUP = 0
        private const val TYPE_SWITCH = 1
    }
}