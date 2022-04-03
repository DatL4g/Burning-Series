package de.datlag.burningseries.adapter

import android.graphics.drawable.Drawable
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
import de.datlag.burningseries.databinding.RecyclerSettingsServiceBinding
import de.datlag.burningseries.databinding.RecyclerSettingsSwitchBinding
import de.datlag.burningseries.extend.RecyclerAdapter
import de.datlag.burningseries.model.SettingsModel
import de.datlag.coilifier.commons.load
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class SettingsRecyclerAdapter : RecyclerAdapter<SettingsModel, SettingsRecyclerAdapter.ViewHolder>() {

    override val diffCallback = object : DiffUtil.ItemCallback<SettingsModel>() {
        override fun areItemsTheSame(oldItem: SettingsModel, newItem: SettingsModel): Boolean {
            return oldItem.isSameItem(newItem)
        }

        override fun areContentsTheSame(oldItem: SettingsModel, newItem: SettingsModel): Boolean {
            return oldItem.isSameContent(newItem)
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
            binding.switcher.setOnCheckedChangeListener(null)
            binding.title.text = item.title
            binding.text.text = item.text
            if (item.text.isEmpty()) {
                binding.text.hide()
            } else {
                binding.text.show()
            }
            binding.switcher.isChecked = item.defaultValue
            binding.switcher.isEnabled = item.enabled
            binding.title.alpha = if (item.enabled) {
                1F
            } else {
                0.5F
            }
            binding.text.alpha = if (item.enabled) {
                1F
            } else {
                0.5F
            }
            binding.switcher.alpha = if (item.enabled) {
                1F
            } else {
                0.5F
            }
            if (item.enabled) {
                binding.switcher.setOnCheckedChangeListener { view, isChecked ->
                    item.listener.invoke(isChecked)
                    view.setOnCheckedChangeListener(null)
                    binding.switcher.setOnCheckedChangeListener(null)
                    submitList(differ.currentList.toMutableList().apply {
                        removeAt(position)
                        add(position, item.apply { defaultValue = isChecked })
                    })
                }
            }
        }

        private fun bindService(item: SettingsModel.Service, position: Int) {
            val binding = RecyclerSettingsServiceBinding.bind(itemView)
            binding.button.setOnClickListener(null)
            item.imageBind.invoke(binding.icon)
            binding.title.text = item.title
            binding.text.text = item.text
            if (item.text.isEmpty()) {
                binding.text.hide()
            } else {
                binding.text.show()
            }
            binding.button.text = item.buttonText
            binding.button.setOnClickListener {
                item.listener.invoke()
            }
        }

        fun bind(settingsModel: SettingsModel, position: Int) {
            when (settingsModel) {
                is SettingsModel.Group -> bindGroup(settingsModel, position)
                is SettingsModel.Switch -> bindSwitch(settingsModel, position)
                is SettingsModel.Service -> bindService(settingsModel, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is SettingsModel.Group -> TYPE_GROUP
            is SettingsModel.Switch -> TYPE_SWITCH
            is SettingsModel.Service -> TYPE_SERVICE
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            TYPE_GROUP -> R.layout.recycler_settings_group
            TYPE_SWITCH -> R.layout.recycler_settings_switch
            TYPE_SERVICE -> R.layout.recycler_settings_service
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
        private const val TYPE_SERVICE = 2
    }
}