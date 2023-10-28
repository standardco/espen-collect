package org.espen.collect.android.preferences.screens

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import org.espen.collect.android.R
import org.espen.collect.android.fragments.dialogs.MovingBackwardsDialog
import org.espen.collect.android.fragments.dialogs.SimpleDialog
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProtectedProjectKeys

class FormEntryAccessPreferencesFragment : org.espen.collect.android.preferences.screens.BaseAdminPreferencesFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.form_entry_access_preferences)

        findPreference(ProtectedProjectKeys.KEY_MOVING_BACKWARDS).onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference, _: Any? ->
                if ((preference as CheckBoxPreference).isChecked) {
                    org.espen.collect.android.fragments.dialogs.MovingBackwardsDialog().show(
                        requireActivity().supportFragmentManager,
                        org.espen.collect.android.fragments.dialogs.MovingBackwardsDialog.MOVING_BACKWARDS_DIALOG_TAG
                    )
                } else {
                    org.espen.collect.android.fragments.dialogs.SimpleDialog.newInstance(
                        requireActivity().getString(org.odk.collect.strings.R.string.moving_backwards_enabled_title),
                        0,
                        requireActivity().getString(org.odk.collect.strings.R.string.moving_backwards_enabled_message),
                        requireActivity().getString(org.odk.collect.strings.R.string.ok),
                        false
                    ).show(
                        requireActivity().supportFragmentManager,
                        org.espen.collect.android.fragments.dialogs.SimpleDialog.COLLECT_DIALOG_TAG
                    )
                    onMovingBackwardsEnabled()
                }
                true
            }

        findPreference(ProtectedProjectKeys.KEY_JUMP_TO).isEnabled =
            settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM)
        findPreference(ProtectedProjectKeys.KEY_SAVE_MID).isEnabled =
            settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM)

        findPreference(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isEnabled =
            settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM) && findPreference(ProtectedProjectKeys.KEY_FINALIZE).isChecked
        findPreference(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference, newValue: Any? ->
                findPreference(ProtectedProjectKeys.KEY_FINALIZE).isEnabled = newValue as Boolean
                true
            }

        findPreference(ProtectedProjectKeys.KEY_FINALIZE).isEnabled = findPreference(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isChecked
        findPreference(ProtectedProjectKeys.KEY_FINALIZE).onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference, newValue: Any? ->
                findPreference(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isEnabled = newValue as Boolean
                true
            }
    }

    fun preventOtherWaysOfEditingForm() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM, false)
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, false)
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, false)
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_FINALIZE, true)
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_JUMP_TO, false)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_CONSTRAINT_BEHAVIOR, ProjectKeys.CONSTRAINT_BEHAVIOR_ON_SWIPE)

        findPreference(ProtectedProjectKeys.KEY_JUMP_TO).isEnabled = false
        findPreference(ProtectedProjectKeys.KEY_SAVE_MID).isEnabled = false
        findPreference(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isEnabled = false
        findPreference(ProtectedProjectKeys.KEY_FINALIZE).isEnabled = false
        findPreference(ProtectedProjectKeys.KEY_JUMP_TO).isChecked = false
        findPreference(ProtectedProjectKeys.KEY_SAVE_MID).isChecked = false
        findPreference(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isChecked = false
        findPreference(ProtectedProjectKeys.KEY_FINALIZE).isChecked = true
    }

    private fun onMovingBackwardsEnabled() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM, true)
        findPreference(ProtectedProjectKeys.KEY_JUMP_TO).isEnabled = true
        findPreference(ProtectedProjectKeys.KEY_SAVE_MID).isEnabled = true
        findPreference(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isEnabled = true
        findPreference(ProtectedProjectKeys.KEY_FINALIZE).isEnabled = true
    }

    private fun findPreference(key: String): CheckBoxPreference {
        return findPreference<Preference>(key) as CheckBoxPreference
    }
}
