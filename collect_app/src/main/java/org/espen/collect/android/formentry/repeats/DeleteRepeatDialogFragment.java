package org.espen.collect.android.formentry.repeats;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.espen.collect.android.formentry.FormEntryViewModel;
import org.espen.collect.android.formentry.audit.AuditEvent;
import org.espen.collect.android.injection.DaggerUtils;
import org.espen.collect.android.javarosawrapper.FormController;

public class DeleteRepeatDialogFragment extends DialogFragment {

    private final ViewModelProvider.Factory viewModelFactory;
    private FormEntryViewModel formEntryViewModel;

    private DeleteRepeatDialogCallback callback;

    public DeleteRepeatDialogFragment(ViewModelProvider.Factory viewModelFactory) {
        this.viewModelFactory = viewModelFactory;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        formEntryViewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(FormEntryViewModel.class);

        if (context instanceof DeleteRepeatDialogCallback) {
            callback = (DeleteRepeatDialogCallback) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        FormController formController = formEntryViewModel.getFormController();

        String name = formController.getLastRepeatedGroupName();
        int repeatCount = formController.getLastRepeatedGroupRepeatCount();
        if (repeatCount != -1) {
            name += " (" + (repeatCount + 1) + ")";
        }

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(getActivity()).create();
        alertDialog.setTitle(getActivity().getString(org.odk.collect.strings.R.string.delete_repeat_ask));
        alertDialog.setMessage(getActivity().getString(org.odk.collect.strings.R.string.delete_repeat_confirm, name));
        DialogInterface.OnClickListener quitListener = (dialog, i) -> {
            if (i == BUTTON_POSITIVE) { // yes
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.DELETE_REPEAT, true, System.currentTimeMillis());
                formController.deleteRepeat();
                callback.deleteGroup();
            }
            alertDialog.cancel();
            dismiss();
        };
        setCancelable(false);
        alertDialog.setCancelable(false);
        alertDialog.setButton(BUTTON_POSITIVE, getActivity().getString(org.odk.collect.strings.R.string.discard_group), quitListener);
        alertDialog.setButton(BUTTON_NEGATIVE, getActivity().getString(org.odk.collect.strings.R.string.delete_repeat_no), quitListener);

        return alertDialog;
    }

    public interface DeleteRepeatDialogCallback {
        void deleteGroup();
    }
}
