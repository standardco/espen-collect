package org.espen.collect.android.preferences.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.CompoundButton
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.espen.collect.android.R
import org.espen.collect.android.databinding.AdminPasswordDialogLayoutBinding
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.preferences.ProjectPreferencesViewModel
import org.espen.collect.android.utilities.AdminPasswordProvider
import org.espen.collect.android.utilities.SoftKeyboardController
import org.odk.collect.androidshared.ui.ToastUtils
import javax.inject.Inject

class AdminPasswordDialogFragment : DialogFragment() {
    @Inject
    lateinit var factory: ProjectPreferencesViewModel.Factory

    @Inject
    lateinit var adminPasswordProvider: AdminPasswordProvider

    @Inject
    lateinit var softKeyboardController: SoftKeyboardController

    lateinit var binding: AdminPasswordDialogLayoutBinding

    lateinit var projectPreferencesViewModel: ProjectPreferencesViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        projectPreferencesViewModel = ViewModelProvider(requireActivity(), factory)[ProjectPreferencesViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AdminPasswordDialogLayoutBinding.inflate(LayoutInflater.from(context))

        binding.editText.post {
            softKeyboardController.showSoftKeyboard(binding.editText)
        }

        binding.checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                binding.editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                binding.editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle(getString(org.odk.collect.strings.R.string.enter_admin_password))
            .setPositiveButton(getString(org.odk.collect.strings.R.string.ok)) { _: DialogInterface?, _: Int ->
                if (adminPasswordProvider.adminPassword == binding.editText.text.toString()) {
                    projectPreferencesViewModel.setStateUnlocked()
                } else {
                    ToastUtils.showShortToast(
                        requireContext(),
                        org.odk.collect.strings.R.string.admin_password_incorrect
                    )
                }
                dismiss()
            }
            .setNegativeButton(getString(org.odk.collect.strings.R.string.cancel)) { _: DialogInterface?, _: Int -> dismiss() }
            .create()
    }
}
