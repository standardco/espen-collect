package org.odk.collect.lists.selects

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

/**
 * An adapter for creating multi select lists with `MultiSelectViewModel` and `RecyclerView`.
 */
class MultiSelectAdapter<T, VH : MultiSelectAdapter.ViewHolder<T>>(
    private val multiSelectViewModel: MultiSelectViewModel<*>,
    private val viewHolderFactory: (ViewGroup) -> VH
) : RecyclerView.Adapter<VH>() {

    var selected: Set<String> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var data = emptyList<SelectItem<T>>()
        set(value) {
            field = value.toList()
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return viewHolderFactory(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.setItem(item.item)

        val checkbox = holder.getCheckbox().also {
            it.isChecked = selected.contains(item.id)
            it.setOnClickListener {
                multiSelectViewModel.toggle(item.id)
            }
        }

        holder.getSelectArea().setOnClickListener {
            checkbox.performClick()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    abstract class ViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun setItem(item: T)
        abstract fun getCheckbox(): CheckBox
        open fun getSelectArea(): View {
            return itemView
        }
    }
}
