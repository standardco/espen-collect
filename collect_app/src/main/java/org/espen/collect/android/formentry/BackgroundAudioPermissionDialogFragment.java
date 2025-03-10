package org.espen.collect.android.formentry;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.espen.collect.android.injection.DaggerUtils;
import org.odk.collect.permissions.PermissionListener;
import org.odk.collect.permissions.PermissionsProvider;

import javax.inject.Inject;

import timber.log.Timber;

public class BackgroundAudioPermissionDialogFragment extends DialogFragment {

    @Inject
    PermissionsProvider permissionsProvider;

    private final ViewModelProvider.Factory viewModelFactory;

    BackgroundAudioViewModel viewModel;

    public BackgroundAudioPermissionDialogFragment(ViewModelProvider.Factory viewModelFactory) {
        this.viewModelFactory = viewModelFactory;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(BackgroundAudioViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setCancelable(false);

        final FragmentActivity activity = requireActivity();
        return new MaterialAlertDialogBuilder(requireContext())
                .setMessage(org.odk.collect.strings.R.string.background_audio_permission_explanation)
                .setPositiveButton(org.odk.collect.strings.R.string.ok, (dialog, which) -> {
                    onOKClicked(activity);
                })
                .create();
    }

    private void onOKClicked(FragmentActivity activity) {
        permissionsProvider.requestRecordAudioPermission(activity, new PermissionListener() {
            @Override
            public void granted() {
                try {
                    viewModel.grantAudioPermission();
                } catch (IllegalStateException e) {
                    Timber.e(e);

                    Toast.makeText(
                            activity,
                            "Could not start recording. Please reopen form.",
                            Toast.LENGTH_LONG
                    ).show();
                    activity.finish();
                }
            }

            @Override
            public void additionalExplanationClosed() {
                activity.finish();
            }
        });
    }
}
