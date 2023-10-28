package org.espen.collect.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.espen.collect.android.databinding.HierarchyElementBinding
import org.espen.collect.android.logic.HierarchyElement
import org.espen.collect.android.utilities.HtmlUtils

class HierarchyListItemView(context: Context) : FrameLayout(context) {

    val binding = HierarchyElementBinding.inflate(LayoutInflater.from(context), this, true)

    fun setElement(element: org.espen.collect.android.logic.HierarchyElement) {
        val icon = element.icon
        if (icon != null) {
            binding.icon.visibility = VISIBLE
            binding.icon.setImageDrawable(icon)
        } else {
            binding.icon.visibility = GONE
        }

        binding.primaryText.text = element.primaryText

        val secondaryText = element.secondaryText
        if (secondaryText != null && secondaryText.isNotEmpty()) {
            binding.secondaryText.visibility = VISIBLE
            binding.secondaryText.text = org.espen.collect.android.utilities.HtmlUtils.textToHtml(secondaryText)
        } else {
            binding.secondaryText.visibility = GONE
        }
    }
}
