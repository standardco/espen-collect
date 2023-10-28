package org.espen.collect.android.preferences.screens

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.analytics.Analytics
import org.espen.collect.android.R
import org.espen.collect.android.activities.ActivityUtils
import org.espen.collect.android.activities.FirstLaunchActivity
import org.espen.collect.android.analytics.AnalyticsEvents
import org.espen.collect.android.configure.qr.QRCodeTabsActivity
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.mainmenu.MainMenuActivity
import org.espen.collect.android.preferences.dialogs.ResetDialogPreference
import org.espen.collect.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat
import org.espen.collect.android.projects.DeleteProjectResult
import org.espen.collect.android.projects.ProjectDeleter
import org.espen.collect.androidshared.ui.ToastUtils
import org.espen.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.settings.keys.ProjectKeys
import javax.inject.Inject

class ProjectManagementPreferencesFragment :
    org.espen.collect.android.preferences.screens.BaseAdminPreferencesFragment(),
    Preference.OnPreferenceClickListener {

    @Inject
    lateinit var projectDeleter: ProjectDeleter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        org.espen.collect.android.injection.DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.project_management_preferences, rootKey)

        val unprotectedSettings = settingsProvider.getUnprotectedSettings()
        val protocol = unprotectedSettings.getString(ProjectKeys.KEY_PROTOCOL)
        if (protocol == ProjectKeys.PROTOCOL_GOOGLE_SHEETS) {
            findPreference<Preference>(IMPORT_SETTINGS_KEY)!!.isVisible = false
        }

        findPreference<Preference>(IMPORT_SETTINGS_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(DELETE_PROJECT_KEY)!!.onPreferenceClickListener = this
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            var resetDialogPreference: org.espen.collect.android.preferences.dialogs.ResetDialogPreference? = null
            if (preference is org.espen.collect.android.preferences.dialogs.ResetDialogPreference) {
                resetDialogPreference = preference
            }
            if (resetDialogPreference != null) {
                val dialogFragment = org.espen.collect.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat.newInstance(preference.key)
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(parentFragmentManager, null)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                IMPORT_SETTINGS_KEY -> {
                    val pref = Intent(activity, QRCodeTabsActivity::class.java)
                    startActivity(pref)
                }
                DELETE_PROJECT_KEY -> {
                    val isGDProject = ProjectKeys.PROTOCOL_GOOGLE_SHEETS == settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_PROTOCOL)
                    val message = if (isGDProject) org.odk.collect.strings.R.string.delete_google_drive_project_confirm_message else org.odk.collect.strings.R.string.delete_project_confirm_message

                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(org.odk.collect.strings.R.string.delete_project)
                        .setMessage(message)
                        .setNegativeButton(org.odk.collect.strings.R.string.delete_project_no) { _: DialogInterface?, _: Int -> }
                        .setPositiveButton(org.odk.collect.strings.R.string.delete_project_yes) { _: DialogInterface?, _: Int -> deleteProject() }
                        .show()
                }
            }
            return true
        }
        return false
    }

    private fun deleteProject() {
        Analytics.log(AnalyticsEvents.DELETE_PROJECT)

        when (val deleteProjectResult = projectDeleter.deleteProject()) {
            is DeleteProjectResult.UnsentInstances -> {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(org.odk.collect.strings.R.string.cannot_delete_project_title)
                    .setMessage(org.odk.collect.strings.R.string.cannot_delete_project_message_one)
                    .setPositiveButton(org.odk.collect.strings.R.string.ok, null)
                    .show()
            }
            is DeleteProjectResult.RunningBackgroundJobs -> {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(org.odk.collect.strings.R.string.cannot_delete_project_title)
                    .setMessage(org.odk.collect.strings.R.string.cannot_delete_project_message_two)
                    .setPositiveButton(org.odk.collect.strings.R.string.ok, null)
                    .show()
            }
            is DeleteProjectResult.DeletedSuccessfullyCurrentProject -> {
                val newCurrentProject = deleteProjectResult.newCurrentProject
                org.espen.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                    requireActivity(),
                    MainMenuActivity::class.java
                )
                ToastUtils.showLongToast(
                    requireContext(),
                    getString(
                        org.odk.collect.strings.R.string.switched_project,
                        newCurrentProject.name
                    )
                )
            }
            is DeleteProjectResult.DeletedSuccessfullyLastProject -> {
                org.espen.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                    requireActivity(),
                    FirstLaunchActivity::class.java
                )
            }
            is DeleteProjectResult.DeletedSuccessfullyInactiveProject -> {
                // not possible here
            }
        }
    }

    companion object {
        const val IMPORT_SETTINGS_KEY = "import_settings"
        const val DELETE_PROJECT_KEY = "delete_project"
    }
}
