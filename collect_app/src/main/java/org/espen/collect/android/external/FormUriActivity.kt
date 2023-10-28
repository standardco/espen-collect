package org.espen.collect.android.external

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.analytics.Analytics
import org.espen.collect.android.activities.FormFillingActivity
import org.espen.collect.android.analytics.AnalyticsEvents
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.instancemanagement.InstanceDeleter
import org.espen.collect.android.instancemanagement.canBeEdited
import org.espen.collect.android.projects.ProjectsDataService
import org.espen.collect.android.utilities.ApplicationConstants
import org.espen.collect.android.utilities.ContentUriHelper
import org.espen.collect.android.utilities.FormsRepositoryProvider
import org.espen.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.forms.Form
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import java.io.File
import javax.inject.Inject

/**
 * This class serves as a firewall for starting form filling. It should be used to do that
 * rather than [FormFillingActivity] directly as it ensures that the required data is valid.
 */
class FormUriActivity : ComponentActivity() {

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var instanceRepositoryProvider: InstancesRepositoryProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    private var formFillingAlreadyStarted = false

    private val openForm =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            setResult(it.resultCode, it.data)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.espen.collect.android.injection.DaggerUtils.getComponent(this).inject(this)

        when {
            !assertProjectListNotEmpty() -> Unit
            !assertCurrentProjectUsed() -> Unit
            !assertValidUri() -> Unit
            !assertFormExists() -> Unit
            !assertNotNewFormInGoogleDriveProject() -> Unit
            !assertFormNotEncrypted() -> Unit
            !assertFormFillingNotAlreadyStarted(savedInstanceState) -> Unit
            else -> startForm()
        }
    }

    private fun assertProjectListNotEmpty(): Boolean {
        val projects = projectsRepository.getAll()
        return if (projects.isEmpty()) {
            displayErrorDialog(getString(org.odk.collect.strings.R.string.app_not_configured))
            false
        } else {
            true
        }
    }

    private fun assertCurrentProjectUsed(): Boolean {
        val projects = projectsRepository.getAll()
        val firstProject = projects.first()
        val uriProjectId = intent.data?.getQueryParameter("projectId")
        val projectId = uriProjectId ?: firstProject.uuid

        return if (projectId != projectsDataService.getCurrentProject().uuid) {
            displayErrorDialog(getString(org.odk.collect.strings.R.string.wrong_project_selected_for_form))
            false
        } else {
            true
        }
    }

    private fun assertNotNewFormInGoogleDriveProject(): Boolean {
        val uri = intent.data!!
        val uriMimeType = contentResolver.getType(uri)
        return if (uriMimeType == org.espen.collect.android.external.InstancesContract.CONTENT_ITEM_TYPE) {
            true
        } else {
            val unprotectedSettings = settingsProvider.getUnprotectedSettings()
            val protocol = unprotectedSettings.getString(ProjectKeys.KEY_PROTOCOL)
            if (ProjectKeys.PROTOCOL_GOOGLE_SHEETS == protocol) {
                displayErrorDialog(getString(org.odk.collect.strings.R.string.cannot_start_new_forms_in_google_drive_projects))
                false
            } else {
                true
            }
        }
    }

    private fun assertValidUri(): Boolean {
        val isUriValid = intent.data?.let {
            val uriMimeType = contentResolver.getType(it)
            if (uriMimeType == null) {
                return@let false
            } else {
                return@let uriMimeType == org.espen.collect.android.external.FormsContract.CONTENT_ITEM_TYPE || uriMimeType == org.espen.collect.android.external.InstancesContract.CONTENT_ITEM_TYPE
            }
        } ?: false

        return if (!isUriValid) {
            displayErrorDialog(getString(org.odk.collect.strings.R.string.unrecognized_uri))
            false
        } else {
            true
        }
    }

    private fun assertFormExists(): Boolean {
        val uri = intent.data!!
        val uriMimeType = contentResolver.getType(uri)

        val doesFormExist = if (uriMimeType == org.espen.collect.android.external.FormsContract.CONTENT_ITEM_TYPE) {
            formsRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))?.let {
                File(it.formFilePath).exists()
            } ?: false
        } else {
            instanceRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))?.let {
                if (!File(it.instanceFilePath).exists()) {
                    Analytics.log(AnalyticsEvents.OPEN_DELETED_INSTANCE)
                    InstanceDeleter(instanceRepositoryProvider.get(), formsRepositoryProvider.get()).delete(it.dbId)
                    displayErrorDialog(getString(org.odk.collect.strings.R.string.instance_deleted_message))
                    return false
                }

                val candidateForms = formsRepositoryProvider.get().getAllByFormIdAndVersion(it.formId, it.formVersion)

                if (candidateForms.isEmpty()) {
                    val version = if (it.formVersion == null) {
                        ""
                    } else {
                        "\n${getString(org.odk.collect.strings.R.string.version)} ${it.formVersion}"
                    }

                    displayErrorDialog(getString(org.odk.collect.strings.R.string.parent_form_not_present, "${it.formId}$version"))
                    return false
                } else if (candidateForms.count { form: Form -> !form.isDeleted } > 1) {
                    displayErrorDialog(getString(org.odk.collect.strings.R.string.survey_multiple_forms_error))
                    return false
                }

                true
            } ?: false
        }

        return if (!doesFormExist) {
            displayErrorDialog(getString(org.odk.collect.strings.R.string.bad_uri))
            false
        } else {
            true
        }
    }

    private fun assertFormNotEncrypted(): Boolean {
        val uri = intent.data!!
        val uriMimeType = contentResolver.getType(uri)

        return if (uriMimeType == org.espen.collect.android.external.InstancesContract.CONTENT_ITEM_TYPE) {
            val instance = instanceRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))
            if (instance!!.canEditWhenComplete()) {
                true
            } else {
                displayErrorDialog(getString(org.odk.collect.strings.R.string.encrypted_form))
                false
            }
        } else {
            true
        }
    }

    private fun assertFormFillingNotAlreadyStarted(savedInstanceState: Bundle?): Boolean {
        if (savedInstanceState != null) {
            formFillingAlreadyStarted = savedInstanceState.getBoolean(FORM_FILLING_ALREADY_STARTED)
        }
        return !formFillingAlreadyStarted
    }

    private fun startForm() {
        formFillingAlreadyStarted = true
        openForm.launch(
            Intent(this, org.espen.collect.android.activities.FormFillingActivity::class.java).apply {
                action = intent.action
                data = intent.data
                intent.extras?.let { sourceExtras -> putExtras(sourceExtras) }
                if (!canFormBeEdited()) {
                    putExtra(org.espen.collect.android.utilities.ApplicationConstants.BundleKeys.FORM_MODE, org.espen.collect.android.utilities.ApplicationConstants.FormModes.VIEW_SENT)
                }
            }
        )
    }

    private fun displayErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setPositiveButton(org.odk.collect.strings.R.string.ok) { _, _ -> finish() }
            .create()
            .show()
    }

    private fun canFormBeEdited(): Boolean {
        val uri = intent.data!!
        val uriMimeType = contentResolver.getType(uri)

        val formEditingEnabled = if (uriMimeType == org.espen.collect.android.external.InstancesContract.CONTENT_ITEM_TYPE) {
            val instance = instanceRepositoryProvider.get().get(ContentUriHelper.getIdFromUri(uri))
            instance!!.canBeEdited(settingsProvider)
        } else {
            true
        }

        return formEditingEnabled
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FORM_FILLING_ALREADY_STARTED, formFillingAlreadyStarted)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val FORM_FILLING_ALREADY_STARTED = "FORM_FILLING_ALREADY_STARTED"
    }
}
