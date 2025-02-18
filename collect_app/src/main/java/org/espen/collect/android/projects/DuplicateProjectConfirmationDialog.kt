package org.odk.collect.android.projects

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.projects.DuplicateProjectConfirmationKeys.MATCHING_PROJECT
import org.odk.collect.android.projects.DuplicateProjectConfirmationKeys.SETTINGS_JSON

class DuplicateProjectConfirmationDialog : DialogFragment() {
    lateinit var listener: DuplicateProjectConfirmationListener

    interface DuplicateProjectConfirmationListener {
        fun createProject(settingsJson: String)
        fun switchToProject(uuid: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Analytics.log(AnalyticsEvents.DUPLICATE_PROJECT)
        listener = parentFragment as DuplicateProjectConfirmationListener

        val settingsJson = arguments?.getString(SETTINGS_JSON, "") ?: ""
        val matchingProject = arguments?.getString(MATCHING_PROJECT, "") ?: ""

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(org.odk.collect.strings.R.string.duplicate_project)
            .setMessage(org.odk.collect.strings.R.string.duplicate_project_details)
            .setPositiveButton(org.odk.collect.strings.R.string.add_duplicate_project) { _, _ -> listener.createProject(settingsJson) }
            .setNegativeButton(org.odk.collect.strings.R.string.switch_to_existing) { _, _ ->
                run {
                    listener.switchToProject(matchingProject)
                    Analytics.log(AnalyticsEvents.DUPLICATE_PROJECT_SWITCH)
                }
            }
            .create()
    }
}

object DuplicateProjectConfirmationKeys {
    const val SETTINGS_JSON = "settingsJson"
    const val MATCHING_PROJECT = "matchingProject"
}
