package org.espen.collect.androidshared.ui

import android.widget.Button
import androidx.lifecycle.ViewModel
import org.espen.collect.androidshared.livedata.MutableNonNullLiveData
import org.espen.collect.androidshared.livedata.NonNullLiveData

class MultiSelectViewModel : ViewModel() {

    private val selected = MutableNonNullLiveData(emptySet<Long>())

    fun select(item: Long) {
        selected.value = selected.value + item
    }

    fun getSelected(): NonNullLiveData<Set<Long>> {
        return selected
    }

    fun unselect(item: Long) {
        selected.value = selected.value - item
    }

    fun unselectAll() {
        selected.value = emptySet()
    }

    fun toggle(item: Long) {
        if (selected.value.contains(item)) {
            unselect(item)
        } else {
            select(item)
        }
    }
}

fun updateSelectAll(button: Button, itemCount: Int, selectedCount: Int): Boolean {
    val allSelected = itemCount > 0 && selectedCount == itemCount

    if (allSelected) {
        button.setText(org.odk.collect.strings.R.string.clear_all)
    } else {
        button.setText(org.odk.collect.strings.R.string.select_all)
    }

    return allSelected
}
