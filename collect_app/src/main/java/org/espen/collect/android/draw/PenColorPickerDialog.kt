package org.espen.collect.android.draw

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rarepebble.colorpicker.ColorPickerView
import org.espen.collect.android.injection.DaggerUtils
import javax.inject.Inject

class PenColorPickerDialog : DialogFragment() {
    @Inject
    lateinit var factory: PenColorPickerViewModel.Factory

    private val model: PenColorPickerViewModel by activityViewModels { factory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        org.espen.collect.android.injection.DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val picker = ColorPickerView(requireContext()).apply {
            color = model.penColor.value
            showAlpha(false)
            showHex(false)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(picker)
            .setTitle(org.odk.collect.strings.R.string.project_color)
            .setPositiveButton(org.odk.collect.strings.R.string.ok) { _, _ -> model.setPenColor(picker.color) }
            .create()
    }
}
