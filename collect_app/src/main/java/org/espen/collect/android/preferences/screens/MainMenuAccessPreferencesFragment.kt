package org.espen.collect.android.preferences.screens

import android.os.Bundle
import androidx.preference.Preference
import org.espen.collect.android.R
import org.espen.collect.android.preferences.utilities.FormUpdateMode
import org.espen.collect.android.preferences.utilities.PreferencesUtils
import org.espen.collect.android.preferences.utilities.SettingsUtils
import org.odk.collect.settings.keys.ProtectedProjectKeys

class MainMenuAccessPreferencesFragment : org.espen.collect.android.preferences.screens.BaseAdminPreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.main_menu_access_preferences, rootKey)

        findPreference<Preference>(ProtectedProjectKeys.KEY_EDIT_SAVED)!!.isEnabled =
            settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM)

        val formUpdateMode = org.espen.collect.android.preferences.utilities.SettingsUtils.getFormUpdateMode(requireContext(), settingsProvider.getUnprotectedSettings())
        if (formUpdateMode == org.espen.collect.android.preferences.utilities.FormUpdateMode.MATCH_EXACTLY) {
            org.espen.collect.android.preferences.utilities.PreferencesUtils.displayDisabled(findPreference(ProtectedProjectKeys.KEY_GET_BLANK), false)
        }
    }
}
