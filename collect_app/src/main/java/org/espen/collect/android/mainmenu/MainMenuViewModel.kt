package org.espen.collect.android.mainmenu

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.espen.collect.android.formmanagement.InstancesAppState
import org.espen.collect.android.instancemanagement.InstanceDiskSynchronizer
import org.espen.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.espen.collect.android.instancemanagement.autosend.shouldFormBeSentAutomatically
import org.espen.collect.android.instancemanagement.canBeEdited
import org.espen.collect.android.preferences.utilities.FormUpdateMode
import org.espen.collect.android.preferences.utilities.SettingsUtils
import org.espen.collect.android.utilities.ContentUriHelper
import org.espen.collect.android.utilities.FormsRepositoryProvider
import org.espen.collect.android.utilities.InstancesRepositoryProvider
import org.espen.collect.android.version.VersionInformation
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.instances.Instance
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

class MainMenuViewModel(
        private val application: Application,
        private val versionInformation: org.espen.collect.android.version.VersionInformation,
        private val settingsProvider: SettingsProvider,
        private val instancesAppState: InstancesAppState,
        private val scheduler: Scheduler,
        private val formsRepositoryProvider: FormsRepositoryProvider,
        private val instancesRepositoryProvider: InstancesRepositoryProvider,
        private val autoSendSettingsProvider: AutoSendSettingsProvider
) : ViewModel() {

    val version: String
        get() = versionInformation.versionToDisplay

    val versionCommitDescription: String?
        get() {
            var commitDescription = ""
            if (versionInformation.commitCount != null) {
                commitDescription =
                    appendToCommitDescription(commitDescription, versionInformation.commitCount.toString())
            }
            if (versionInformation.commitSHA != null) {
                commitDescription =
                    appendToCommitDescription(commitDescription, versionInformation.commitSHA!!)
            }
            if (versionInformation.isDirty) {
                commitDescription = appendToCommitDescription(commitDescription, "dirty")
            }
            return if (commitDescription.isNotEmpty()) {
                commitDescription
            } else {
                null
            }
        }

    fun shouldEditSavedFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
    }

    fun shouldSendFinalizedFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SEND_FINALIZED)
    }

    fun shouldViewSentFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_VIEW_SENT)
    }

    fun shouldGetBlankFormButtonBeVisible(): Boolean {
        val buttonEnabled = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_GET_BLANK)
        return !isMatchExactlyEnabled() && buttonEnabled
    }

    fun shouldDeleteSavedFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_DELETE_SAVED)
    }

    private fun isMatchExactlyEnabled(): Boolean {
        return org.espen.collect.android.preferences.utilities.SettingsUtils.getFormUpdateMode(application, settingsProvider.getUnprotectedSettings()) == org.espen.collect.android.preferences.utilities.FormUpdateMode.MATCH_EXACTLY
    }

    private fun appendToCommitDescription(commitDescription: String, part: String): String {
        return if (commitDescription.isEmpty()) {
            part
        } else {
            "$commitDescription-$part"
        }
    }

    fun refreshInstances() {
        scheduler.immediate<Any?>({
            org.espen.collect.android.instancemanagement.InstanceDiskSynchronizer(settingsProvider).doInBackground()
            instancesAppState.update()
            null
        }) { }
    }

    val editableInstancesCount: LiveData<Int>
        get() = instancesAppState.editableCount

    val sendableInstancesCount: LiveData<Int>
        get() = instancesAppState.sendableCount

    val sentInstancesCount: LiveData<Int>
        get() = instancesAppState.sentCount

    fun getFormSavedSnackbarDetails(uri: Uri): Pair<Int, Int?>? {
        val instance = instancesRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))
        return if (instance != null) {
            val message = if (instance.status == Instance.STATUS_INCOMPLETE) {
                org.odk.collect.strings.R.string.form_saved_as_draft
            } else if (instance.status == Instance.STATUS_COMPLETE) {
                val form = formsRepositoryProvider.get().getAllByFormIdAndVersion(instance.formId, instance.formVersion).first()
                if (form.shouldFormBeSentAutomatically(autoSendSettingsProvider.isAutoSendEnabledInSettings())) {
                    org.odk.collect.strings.R.string.form_sending
                } else {
                    org.odk.collect.strings.R.string.form_saved
                }
            } else {
                return null
            }

            val action = if (instance.canBeEdited(settingsProvider)) {
                org.odk.collect.strings.R.string.edit_form
            } else {
                if (instance.status == Instance.STATUS_INCOMPLETE || instance.canEditWhenComplete()) {
                    org.odk.collect.strings.R.string.view_form
                } else {
                    null
                }
            }

            return Pair(message, action)
        } else {
            null
        }
    }
}
