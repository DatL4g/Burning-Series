package de.datlag.burningseries.ui.dialog

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import de.datlag.burningseries.R
import de.datlag.burningseries.common.gone
import de.datlag.burningseries.common.materialDialogBuilder
import de.datlag.burningseries.common.visible
import de.datlag.burningseries.databinding.StreamConfigTimeEditorBinding
import de.datlag.model.burningseries.stream.StreamConfig
import io.michaelrocks.paranoid.Obfuscate
import java.util.concurrent.TimeUnit

@Obfuscate
data class TimeEditDialog(
    val onClose: (config: StreamConfig) -> Unit,
    val currentPosition: () -> Long?
) {
    private lateinit var dialog: AlertDialog
    private lateinit var config: StreamConfig

    private var _binding: StreamConfigTimeEditorBinding? = null
    private val binding: StreamConfigTimeEditorBinding
        get() = _binding!!

    private var previousButtonId: Int? = null

    private fun millisToHoursMinutesSeconds(millis: Long): Triple<Int, Int, Int> {
        return Triple(
            (((millis / 1000) / (60 * 60)) % 24).toInt(),
            (((millis / 1000) / 60) % 60).toInt(),
            ((millis / 1000) % 60).toInt()
        )
    }

    private fun initViews(
        duration: Triple<Int, Int, Int>,
        visibilities: Triple<Boolean, Boolean, Boolean> = duration.run {
            Triple(
                first > 0,
                second > 0 || first > 0,
                third > 0 || second > 0 || first > 0
            )
        }
    ) = with(binding) {
        if (visibilities.first) {
            startHourTextField.visible()
            endHourTextField.visible()
        } else {
            startHourTextField.gone()
            endHourTextField.gone()
        }

        if (visibilities.second) {
            startMinuteTextField.visible()
            endMinuteTextField.visible()
        } else {
            startMinuteTextField.gone()
            endMinuteTextField.gone()
        }

        if (visibilities.third) {
            startSecondTextField.visible()
            endSecondTextField.visible()
        } else {
            startSecondTextField.gone()
            endSecondTextField.gone()
        }

        if (previousButtonId != null && previousButtonId != View.NO_ID) {
            toggleButton.check(previousButtonId!!)
        }

        startApply.setOnClickListener {
            val current = currentPosition.invoke() ?: getSelectedConfigStart() ?: 0

            applyTimeToStartEditTexts(millisToHoursMinutesSeconds(current))
        }

        endApply.setOnClickListener {
            val current = currentPosition.invoke() ?: getSelectedConfigEnd() ?: 0

            applyTimeToEndEditTexts(millisToHoursMinutesSeconds(current))
        }

        startSecondTextField.setMaxNumber(duration)
        startMinuteTextField.setMaxNumber(duration)
        startHourTextField.setMaxNumber(duration)

        endSecondTextField.setMaxNumber(duration)
        endMinuteTextField.setMaxNumber(duration)
        endHourTextField.setMaxNumber(duration)

        applyConfigToEditTexts()
        toggleButton.addOnButtonCheckedListener { _, _, _ ->
            applyConfigToEditTexts()
        }
    }

    private fun isThrowbackSelected(): Boolean =
        binding.toggleButton.checkedButtonId == R.id.throwbackButton

    private fun isIntroSelected(): Boolean =
        binding.toggleButton.checkedButtonId == R.id.introButton

    private fun isOutroSelected(): Boolean =
        binding.toggleButton.checkedButtonId == R.id.outroButton

    private fun applyConfigToEditTexts() {
        val selectedEnd = getSelectedConfigEnd()
        val selectedStart = getSelectedConfigStart()

        if (selectedEnd != null) {
            applyTimeToEndEditTexts(millisToHoursMinutesSeconds(selectedEnd))
        } else {
            clearEndTimeEditTexts()
        }

        if (selectedStart != null) {
            applyTimeToStartEditTexts(millisToHoursMinutesSeconds(selectedStart))
        } else {
            clearStartTimeEditTexts()
        }
    }

    private fun getSelectedConfigStart(): Long? = when {
        isThrowbackSelected() -> config.throwback.start
        isIntroSelected() -> config.intro.start
        isOutroSelected() -> config.outro.start
        else -> null
    }

    private fun getSelectedConfigEnd(): Long? = when {
        isThrowbackSelected() -> config.throwback.end
        isIntroSelected() -> config.intro.end
        isOutroSelected() -> config.outro.end
        else -> null
    }

    private fun TextInputLayout.setMaxNumber(
        duration: Triple<Int, Int, Int>
    ) {
        this.editText?.addTextChangedListener {
            checkEditTextValid(duration)
        }
    }

    private fun checkEditTextValid(duration: Triple<Int, Int, Int>) = with(binding) {
        val startHourValue = startHourTextField.editText?.text?.toString()?.toIntOrNull()
        val startMinuteValue = startMinuteTextField.editText?.text?.toString()?.toIntOrNull()
        val startSecondValue = startSecondTextField.editText?.text?.toString()?.toIntOrNull()

        val hourMaxValue = duration.first
        val minuteMaxValue = (if (duration.first > 0) 59 else duration.second)
        val secondMaxValue = (if (duration.first > 0 || duration.second > 0) 59 else duration.third)

        val startHourValueError = if (startHourValue != null) {
            startHourValue > hourMaxValue
        } else false
        val startMinuteValueError = if (startMinuteValue != null) {
            startMinuteValue > minuteMaxValue
        } else false
        val startSecondValueError = if (startSecondValue != null) {
            startSecondValue > secondMaxValue
        } else false

        if (startHourValueError) {
            startHourTextField.error = startHourTextField.context.getString(R.string.max_number, hourMaxValue)
        } else {
            startHourTextField.error = null
        }
        if (startMinuteValueError) {
            startMinuteTextField.error = startMinuteTextField.context.getString(R.string.max_number, minuteMaxValue)
        } else {
            startMinuteTextField.error = null
        }
        if (startSecondValueError) {
            startSecondTextField.error = startSecondTextField.context.getString(R.string.max_number, secondMaxValue)
        } else {
            startSecondTextField.error = null
        }



        val endHourValue = endHourTextField.editText?.text?.toString()?.toIntOrNull()
        val endMinuteValue = endMinuteTextField.editText?.text?.toString()?.toIntOrNull()
        val endSecondValue = endSecondTextField.editText?.text?.toString()?.toIntOrNull()

        val endHourValueError = if (endHourValue != null) {
            endHourValue > hourMaxValue
        } else false
        val endMinuteValueError = if (endMinuteValue != null) {
            endMinuteValue > minuteMaxValue
        } else false
        val endSecondValueError = if (endSecondValue != null) {
            endSecondValue > secondMaxValue
        } else false

        fun applyEndErrorValueMessage() {
            if (endHourValueError) {
                endHourTextField.error = endHourTextField.context.getString(R.string.max_number, hourMaxValue)
            } else {
                endHourTextField.error = null
            }
            if (endMinuteValueError) {
                endMinuteTextField.error = endMinuteTextField.context.getString(R.string.max_number, minuteMaxValue)
            } else {
                endMinuteTextField.error = null
            }
            if (endSecondValueError) {
                endSecondTextField.error = endSecondTextField.context.getString(R.string.max_number, secondMaxValue)
            } else {
                endSecondTextField.error = null
            }
        }
        applyEndErrorValueMessage()

        val newEnd = getValidNewTime(getEndEditTextAsMillis(), false)
        val newStart = getValidNewTime(getStartEditTextAsMillis(), true)

        if (newStart != null && newEnd != null) {
            if (newStart + 10000 > newEnd) {
                endHourTextField.error = endHourTextField.context.getString(R.string.too_low)
                endMinuteTextField.error = endMinuteTextField.context.getString(R.string.too_low)
                endSecondTextField.error = endSecondTextField.context.getString(R.string.too_low)
            } else {
                applyEndErrorValueMessage()
            }
        } else {
            applyEndErrorValueMessage()
        }

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val hasError = isErroneous()
        positiveButton.isEnabled = !hasError
        positiveButton.isClickable = !hasError
        val drawables = positiveButton.compoundDrawables
        drawables.forEach {
            it?.clearColorFilter()
            it?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(positiveButton.currentTextColor, BlendModeCompat.SRC_IN)
        }
        positiveButton.setCompoundDrawables(drawables.getOrNull(0), drawables.getOrNull(1), drawables.getOrNull(2), drawables.getOrNull(3))
    }

    private fun isErroneous(): Boolean = with(binding) {
        return startHourTextField.error != null
                || startMinuteTextField.error != null
                || startSecondTextField.error != null
                || endHourTextField.error != null
                || endMinuteTextField.error != null
                || endSecondTextField.error != null
    }

    private fun applyTimeToStartEditTexts(value: Triple<Int, Int, Int>) = with(binding) {
        if (startHourTextField.isVisible) {
            startHourTextField.editText?.setText(value.first.toString())
        } else {
            startHourTextField.editText?.text = null
        }
        if (startMinuteTextField.isVisible) {
            startMinuteTextField.editText?.setText(value.second.toString())
        } else {
            startMinuteTextField.editText?.text = null
        }
        startSecondTextField.editText?.setText(value.third.toString())
    }

    private fun applyTimeToEndEditTexts(value: Triple<Int, Int, Int>) = with(binding) {
        if (endHourTextField.isVisible) {
            endHourTextField.editText?.setText(value.first.toString())
        } else {
            endHourTextField.editText?.text = null
        }
        if (endMinuteTextField.isVisible) {
            endMinuteTextField.editText?.setText(value.second.toString())
        } else {
            endMinuteTextField.editText?.text = null
        }
        endSecondTextField.editText?.setText(value.third.toString())
    }

    private fun clearStartTimeEditTexts() = with(binding) {
        startHourTextField.editText?.text = null
        startMinuteTextField.editText?.text = null
        startSecondTextField.editText?.text = null
    }

    private fun clearEndTimeEditTexts() = with(binding) {
        endHourTextField.editText?.text = null
        endMinuteTextField.editText?.text = null
        endSecondTextField.editText?.text = null
    }

    private fun getValidNewTime(
        values: Triple<Long?, Long?, Long?>,
        isStart: Boolean
    ) = getValidNewTime(values.first ?: -1, values.second ?: -1, values.third ?: -1, isStart, false)

    private fun getValidNewTime(
        newHourMillis: Long,
        newMinuteMillis: Long,
        newSecondMillis: Long,
        isStart: Boolean,
        returnConfigValues: Boolean
    ): Long? {
        val returnVal = if (newHourMillis > -1L && newMinuteMillis > -1L && newSecondMillis > -1L) {
            newHourMillis + newMinuteMillis + newSecondMillis
        } else if (-1L in newHourMillis until newMinuteMillis && newSecondMillis > -1L) {
            newMinuteMillis + newSecondMillis
        } else if (newHourMillis <= -1L && newMinuteMillis <= -1L && newSecondMillis > -1L) {
            newSecondMillis
        } else if (newHourMillis <= -1L && newMinuteMillis <= -1L && newSecondMillis <= -1L) {
            if (returnConfigValues) {
                if (isStart) getSelectedConfigStart() else getSelectedConfigEnd()
            } else {
                null
            }
        } else {
            val hours = if (newHourMillis <= -1L) 0 else newHourMillis
            val minutes = if (newMinuteMillis <= -1L) 0 else newMinuteMillis
            val seconds = if (newSecondMillis <= -1L) 0 else newSecondMillis
            hours + minutes + seconds
        }
        return if (returnVal != null && returnVal < 0) {
            null
        } else {
            returnVal
        }
    }

    private fun getStartEditTextAsMillis(): Triple<Long?, Long?, Long?> = with(binding) {
        Triple(
            startHourTextField.editText?.text?.toString()?.toLongOrNull()?.let { TimeUnit.HOURS.toMillis(it) },
            startMinuteTextField.editText?.text?.toString()?.toLongOrNull()?.let { TimeUnit.MINUTES.toMillis(it) },
            startSecondTextField.editText?.text?.toString()?.toLongOrNull()?.let { TimeUnit.SECONDS.toMillis(it) }
        )
    }

    private fun calculateNewConfigStart(): Long? = with(binding) {
        val (editHrs, editMin, editSec) = getStartEditTextAsMillis()

        getValidNewTime(editHrs ?: -1, editMin ?: -1, editSec ?: -1,
            isStart = true,
            returnConfigValues = true
        )
    }

    private fun getEndEditTextAsMillis(): Triple<Long?, Long?, Long?> = with(binding) {
        Triple(
            endHourTextField.editText?.text?.toString()?.toLongOrNull()?.let { TimeUnit.HOURS.toMillis(it) },
            endMinuteTextField.editText?.text?.toString()?.toLongOrNull()?.let { TimeUnit.MINUTES.toMillis(it) },
            endSecondTextField.editText?.text?.toString()?.toLongOrNull()?.let { TimeUnit.SECONDS.toMillis(it) }
        )
    }

    private fun calculateNewConfigEnd(): Long? = with(binding) {
        val (editHrs, editMin, editSec) = getEndEditTextAsMillis()

        getValidNewTime(editHrs ?: -1, editMin ?: -1, editSec ?: -1,
            isStart = false,
            returnConfigValues = true
        )
    }

    fun create(context: Context, streamConfig: StreamConfig) {
        if (!::config.isInitialized || config != streamConfig) {
            config = streamConfig.newInstance()
        }

        dialog = context.materialDialogBuilder {
            setPositiveButtonIcon(R.drawable.ic_baseline_check_24)
            setNegativeButtonIcon(R.drawable.ic_baseline_close_24)
            builder {
                setTitle(R.string.edit_timestamps)
                setView(R.layout.stream_config_time_editor)
                setPositiveButton(R.string.done) { dialog, _ ->
                    when {
                        isThrowbackSelected() -> {
                            config.throwback.start = calculateNewConfigStart()
                            config.throwback.end = calculateNewConfigEnd()
                        }
                        isIntroSelected() -> {
                            config.intro.start = calculateNewConfigStart()
                            config.intro.end = calculateNewConfigEnd()
                        }
                        isOutroSelected() -> {
                            config.outro.start = calculateNewConfigStart()
                            config.outro.end = calculateNewConfigEnd()
                        }
                    }
                    dialog.dismiss()
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                setOnDismissListener {
                    previousButtonId = binding.toggleButton.checkedButtonId
                    onClose.invoke(config)
                }
                setOnCancelListener {
                    previousButtonId = binding.toggleButton.checkedButtonId
                    onClose.invoke(config)
                }
            }
        }
    }

    fun show(videoDuration: Long) {
        dialog.show()

        if (_binding == null) {
            dialog.findViewById<View>(R.id.timeEditRoot)?.let {
                _binding = StreamConfigTimeEditorBinding.bind(it)
            }
            initViews(millisToHoursMinutesSeconds(videoDuration))
        }
    }

    fun creationRequired(): Boolean {
        return !::dialog.isInitialized || !::config.isInitialized
    }
}
