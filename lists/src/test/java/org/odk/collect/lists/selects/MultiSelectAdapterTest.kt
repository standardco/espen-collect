package org.odk.collect.lists.selects

import android.content.Context
import android.widget.FrameLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.lists.selects.support.TextAndCheckBoxViewHolder

@RunWith(AndroidJUnit4::class)
class MultiSelectAdapterTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `selected items are checked`() {
        val data = MutableLiveData(listOf(SelectItem<Long>("1", 1), SelectItem<Long>("2", 2)))
        val multiSelectViewModel = MultiSelectViewModel(data)
        multiSelectViewModel.select("1")

        val adapter = MultiSelectAdapter(multiSelectViewModel) {
            TextAndCheckBoxViewHolder<Long>(it.context)
        }

        adapter.data = multiSelectViewModel.getData().value!!
        adapter.selected = multiSelectViewModel.getSelected().value

        val holders = createAndBindList(adapter)
        assertThat(holders.size, equalTo(2))
        assertThat(holders[0].view.checkBox.isChecked, equalTo(true))
        assertThat(holders[1].view.checkBox.isChecked, equalTo(false))
    }

    @Test
    fun `checking an item selects it`() {
        val data = MutableLiveData(listOf(SelectItem<Long>("1", 1), SelectItem<Long>("2", 2)))
        val multiSelectViewModel = MultiSelectViewModel(data)

        val adapter = MultiSelectAdapter(multiSelectViewModel) {
            TextAndCheckBoxViewHolder<Long>(it.context)
        }

        adapter.data = multiSelectViewModel.getData().value!!
        adapter.selected = multiSelectViewModel.getSelected().value

        val holders = createAndBindList(adapter)
        holders[0].view.checkBox.performClick()
        assertThat(multiSelectViewModel.getSelected().value, equalTo(setOf("1")))
    }

    @Test
    fun `clicking an item selects it`() {
        val data = MutableLiveData(listOf(SelectItem<Long>("1", 1), SelectItem<Long>("2", 2)))
        val multiSelectViewModel = MultiSelectViewModel(data)

        val adapter = MultiSelectAdapter(multiSelectViewModel) {
            TextAndCheckBoxViewHolder<Long>(it.context)
        }

        adapter.data = multiSelectViewModel.getData().value!!
        adapter.selected = multiSelectViewModel.getSelected().value

        val holders = createAndBindList(adapter)
        holders[0].view.performClick()
        assertThat(multiSelectViewModel.getSelected().value, equalTo(setOf("1")))
    }

    @Test
    fun `unchecking an item selects it`() {
        val data = MutableLiveData(listOf(SelectItem<Long>("1", 1), SelectItem<Long>("2", 2)))
        val multiSelectViewModel = MultiSelectViewModel(data)
        multiSelectViewModel.select("1")

        val adapter = MultiSelectAdapter(multiSelectViewModel) {
            TextAndCheckBoxViewHolder<Long>(it.context)
        }

        adapter.data = multiSelectViewModel.getData().value!!
        adapter.selected = multiSelectViewModel.getSelected().value

        val holders = createAndBindList(adapter)
        holders[0].view.checkBox.performClick()
        assertThat(multiSelectViewModel.getSelected().value, equalTo(setOf()))
    }

    private fun <T : ViewHolder> createAndBindList(adapter: RecyclerView.Adapter<T>): List<T> {
        return 0.rangeUntil(adapter.itemCount).map { position ->
            adapter.onCreateViewHolder(FrameLayout(context), position).also { holder ->
                adapter.onBindViewHolder(holder, position)
            }
        }
    }
}
