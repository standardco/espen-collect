package org.odk.collect.android.formlists.blankformlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.lists.RecyclerViewUtils
import org.odk.collect.lists.RecyclerViewUtils.matchParentWidth
import org.odk.collect.lists.selects.MultiSelectAdapter
import org.odk.collect.lists.selects.MultiSelectControlsFragment
import org.odk.collect.lists.selects.MultiSelectListFragment
import org.odk.collect.lists.selects.MultiSelectViewModel
import org.odk.collect.lists.selects.SelectItem
import org.odk.collect.strings.R.string

class DeleteBlankFormFragment(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val menuHost: MenuHost
) : Fragment() {

    private val blankFormListViewModel: BlankFormListViewModel by viewModels { viewModelFactory }
    private val multiSelectViewModel: MultiSelectViewModel<BlankFormListItem> by viewModels {
        MultiSelectViewModel.Factory(
            blankFormListViewModel.formsToDisplay.map {
                it.map { blankForm ->
                    SelectItem(
                        blankForm.databaseId.toString(),
                        blankForm
                    )
                }
            }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(MultiSelectListFragment::class) {
                MultiSelectListFragment(
                    getString(string.delete_file),
                    multiSelectViewModel,
                    ::SelectableBlankFormListItemViewHolder
                ) {
                    it.empty.setIcon(R.drawable.ic_baseline_delete_72)
                    it.empty.setTitle(getString(string.empty_list_of_forms_to_delete_title))
                    it.empty.setSubtitle(getString(string.empty_list_of_blank_forms_to_delete_subtitle))

                    it.list.addItemDecoration(RecyclerViewUtils.verticalLineDivider(context))
                }
            }
            .build()

        childFragmentManager.setFragmentResultListener(
            MultiSelectControlsFragment.REQUEST_ACTION,
            this
        ) { _, result ->
            val selected = result.getStringArray(MultiSelectControlsFragment.RESULT_SELECTED)!!
            onDeleteSelected(selected.map { it.toLong() }.toLongArray())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.delete_form_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val blankFormListMenuProvider =
            BlankFormListMenuProvider(requireActivity(), blankFormListViewModel)
        menuHost.addMenuProvider(blankFormListMenuProvider, viewLifecycleOwner, State.RESUMED)
    }

    private fun onDeleteSelected(selected: LongArray) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(string.delete_file)
            .setMessage(
                getString(
                    string.delete_confirm,
                    selected.size.toString()
                )
            )
            .setPositiveButton(getString(string.delete_yes)) { _, _ ->
                blankFormListViewModel.deleteForms(*selected)
                multiSelectViewModel.unselectAll()
            }
            .setNegativeButton(getString(string.delete_no), null)
            .show()
    }
}

private class SelectableBlankFormListItemViewHolder(parent: ViewGroup) :
    MultiSelectAdapter.ViewHolder<BlankFormListItem>(
        BlankFormListItemView(parent.context).also {
            it.setTrailingView(R.layout.checkbox)
        }
    ) {

    init {
        matchParentWidth()
    }

    override fun setItem(item: BlankFormListItem) {
        (itemView as BlankFormListItemView).setItem(item)
    }

    override fun getCheckbox(): CheckBox {
        return itemView.findViewById(R.id.checkbox)
    }
}
