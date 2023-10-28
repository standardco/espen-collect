package org.espen.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.espen.collect.android.injection.DaggerUtils;
import org.espen.collect.android.preferences.source.SettingsStore;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class BaseAdminPreferencesFragment extends BasePreferencesFragment {

    @Inject
    @Named("ADMIN_SETTINGS_STORE")
    SettingsStore adminSettingsStore;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setPreferenceDataStore(adminSettingsStore);
    }

    @Override
    public void onResume() {
        super.onResume();
        settingsProvider.getProtectedSettings().registerOnSettingChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        settingsProvider.getProtectedSettings().unregisterOnSettingChangeListener(this);
    }

    @Override
    public void onSettingChanged(@NotNull String key) {
        settingsChangeHandler.onSettingChanged(projectsDataService.getCurrentProject().getUuid(), settingsProvider.getProtectedSettings().getAll().get(key), key);
    }
}
