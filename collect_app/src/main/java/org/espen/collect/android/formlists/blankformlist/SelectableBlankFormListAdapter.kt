package org.espen.collect.android.formlists.blankformlist

import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import org.espen.collect.android.R

class SelectableBlankFormListAdapter(private val onItemClickListener: (Long) -> Unit) :
    RecyclerView.Adapter<BlankFormListItemViewHolder>() {

    var selected: Set<Long> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var formItems = emptyList<BlankFormListItem>()
        set(value) {
            field = value.toList()
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlankFormListItemViewHolder {
        return BlankFormListItemViewHolder(parent).also {
            it.setTrailingView(R.layout.checkbox)
        }
    }

    override fun onBindViewHolder(holder: BlankFormListItemViewHolder, position: Int) {
        val item = formItems[position]
        holder.blankFormListItem = item

        val checkbox = holder.itemView.findViewById<CheckBox>(R.id.checkbox).also {
            it.isChecked = selected.contains(item.databaseId)
            it.setOnClickListener {
                onItemClickListener(item.databaseId)
            }
        }

        holder.itemView.setOnClickListener {
            checkbox.performClick()
        }
    }

    override fun getItemCount() = formItems.size
}
