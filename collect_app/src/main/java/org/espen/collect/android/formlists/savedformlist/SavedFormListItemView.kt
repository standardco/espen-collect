package org.espen.collect.android.formlists.savedformlist

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import org.espen.collect.android.R
import org.espen.collect.android.databinding.FormChooserListItemMultipleChoiceBinding
import org.espen.collect.android.instancemanagement.getIcon
import org.espen.collect.android.instancemanagement.getStatusDescription
import org.odk.collect.forms.instances.Instance
import java.util.Date

class SavedFormListItemView(context: Context) : FrameLayout(context) {

    val binding =
        FormChooserListItemMultipleChoiceBinding.inflate(LayoutInflater.from(context), this, true)

    fun setItem(value: Instance) {
        val lastStatusChangeDate = value.lastStatusChangeDate
        val status = value.status

        binding.root.findViewById<TextView>(R.id.form_title).text = value.displayName
        binding.root.findViewById<TextView>(R.id.form_subtitle).text =
            getStatusDescription(context, status, Date(lastStatusChangeDate))

        val statusIcon = binding.root.findViewById<ImageView>(R.id.image)
        statusIcon.setImageResource(value.getIcon())
    }
}
