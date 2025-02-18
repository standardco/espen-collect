package org.odk.collect.geo.selection

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import org.odk.collect.geo.R
import org.odk.collect.geo.databinding.PropertyBinding
import org.odk.collect.geo.databinding.SelectionSummarySheetLayoutBinding

internal class SelectionSummarySheet(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    val binding =
        SelectionSummarySheetLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    var listener: Listener? = null

    val peekHeight: Int
        get() {
            return when (binding.properties.size) {
                0 -> binding.properties.top
                1 -> binding.properties.top + binding.properties[0].bottom
                else -> {
                    val bottomOfFirstProp = binding.properties.top + binding.properties[0].bottom
                    val secondPropHeight = binding.properties[1].bottom - binding.properties[1].top
                    val enoughOfSecondPropToImplyMoreProps = (secondPropHeight / 12) * 7
                    bottomOfFirstProp + enoughOfSecondPropToImplyMoreProps
                }
            }
        }

    private var itemId: Long? = null

    init {
        binding.action.setOnClickListener {
            itemId?.let { listener?.selectionAction(it) }
        }
    }

    fun setItem(item: MappableSelectItem) {
        itemId = item.id
        when (item.status) {
            Status.ERRORS -> binding.statusChip.errors = true
            Status.NO_ERRORS -> binding.statusChip.errors = false
            else -> binding.statusChip.visibility = View.GONE
        }

        binding.name.text = item.name

        binding.properties.removeAllViews()
        item.properties.forEach {
            val property = PropertyBinding.bind(
                LayoutInflater.from(context).inflate(R.layout.property, binding.properties, false)
            )

            property.text.text = it.text

            if (it.icon != null) {
                property.icon.setImageDrawable(ContextCompat.getDrawable(context, it.icon))
                property.icon.background = null
            } else {
                property.icon.visibility = View.GONE
            }

            binding.properties.addView(property.root)
        }

        item.action?.let {
            binding.action.text = it.text

            if (it.icon != null) {
                binding.action.icon = ContextCompat.getDrawable(context, it.icon)
            }

            binding.action.visibility = View.VISIBLE
        }

        item.info?.let {
            binding.info.text = item.info
            binding.info.visibility = View.VISIBLE
        }
    }

    interface Listener {
        fun selectionAction(id: Long)
    }
}
