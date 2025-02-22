package org.espen.collect.android.instancemanagement

import org.odk.collect.analytics.Analytics
import org.espen.collect.android.analytics.AnalyticsEvents
import org.espen.collect.android.application.EspenCollect
import org.espen.collect.android.gdrive.GoogleAccountsManager
import org.espen.collect.android.gdrive.GoogleApiProvider
import org.espen.collect.android.gdrive.InstanceGoogleSheetsUploader
import org.espen.collect.android.upload.FormUploadException
import org.espen.collect.android.upload.InstanceServerUploader
import org.espen.collect.android.upload.InstanceUploader
import org.espen.collect.android.utilities.FormsRepositoryProvider
import org.espen.collect.android.utilities.InstanceAutoDeleteChecker
import org.espen.collect.android.utilities.InstanceUploaderUtils
import org.espen.collect.android.utilities.InstancesRepositoryProvider
import org.espen.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.metadata.PropertyManager.Companion.PROPMGR_DEVICE_ID
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import timber.log.Timber

class InstanceSubmitter(
        private val formsRepository: FormsRepository,
        private val googleAccountsManager: org.espen.collect.android.gdrive.GoogleAccountsManager,
        private val googleApiProvider: org.espen.collect.android.gdrive.GoogleApiProvider,
        private val permissionsProvider: PermissionsProvider,
        private val generalSettings: Settings,
        private val propertyManager: PropertyManager
) {

    @Throws(SubmitException::class)
    fun submitInstances(toUpload: List<Instance>): Map<Instance, FormUploadException?> {
        if (toUpload.isEmpty()) {
            throw SubmitException(SubmitException.Type.NOTHING_TO_SUBMIT)
        }
        val result = mutableMapOf<Instance, FormUploadException?>()
        val deviceId = propertyManager.getSingularProperty(PROPMGR_DEVICE_ID)

        val uploader: org.espen.collect.android.upload.InstanceUploader = if (isGoogleSheetsProtocol()) {
            setUpGoogleSheetsUploader()
        } else {
            setUpODKUploader()
        }

        for (instance in toUpload) {
            try {
                var destinationUrl: String
                if (isGoogleSheetsProtocol()) {
                    destinationUrl = uploader.getUrlToSubmitTo(
                        instance,
                        null,
                        null,
                        generalSettings.getString(ProjectKeys.KEY_GOOGLE_SHEETS_URL)
                    )
                    if (!org.espen.collect.android.utilities.InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile(destinationUrl)) {
                        result[instance] = FormUploadException(org.espen.collect.android.utilities.InstanceUploaderUtils.SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE)
                        continue
                    }
                } else {
                    destinationUrl = uploader.getUrlToSubmitTo(instance, deviceId, null, null)
                }
                uploader.uploadOneSubmission(instance, destinationUrl)
                result[instance] = null

                deleteInstance(instance)
                logUploadedForm(instance)
            } catch (e: FormUploadException) {
                Timber.d(e)
                result[instance] = e
            }
        }
        return result
    }

    @Throws(SubmitException::class)
    private fun setUpGoogleSheetsUploader(): org.espen.collect.android.upload.InstanceUploader {
        if (permissionsProvider.isGetAccountsPermissionGranted) {
            val googleUsername = googleAccountsManager.lastSelectedAccountIfValid
            if (googleUsername.isEmpty()) {
                throw SubmitException(SubmitException.Type.GOOGLE_ACCOUNT_NOT_SET)
            }
            googleAccountsManager.selectAccount(googleUsername)
            return org.espen.collect.android.gdrive.InstanceGoogleSheetsUploader(
                    googleApiProvider.getDriveApi(googleUsername),
                    googleApiProvider.getSheetsApi(googleUsername)
            )
        } else {
            throw SubmitException(SubmitException.Type.GOOGLE_ACCOUNT_NOT_PERMITTED)
        }
    }

    private fun setUpODKUploader(): org.espen.collect.android.upload.InstanceUploader {
        val httpInterface = org.espen.collect.android.application.EspenCollect.getInstance().component.openRosaHttpInterface()
        return org.espen.collect.android.upload.InstanceServerUploader(
                httpInterface,
                org.espen.collect.android.utilities.WebCredentialsUtils(generalSettings),
                generalSettings
        )
    }

    private fun isGoogleSheetsProtocol() =
        generalSettings.getString(ProjectKeys.KEY_PROTOCOL) == ProjectKeys.PROTOCOL_GOOGLE_SHEETS

    private fun deleteInstance(instance: Instance) {
        // If the submission was successful, delete the instance if either the app-level
        // delete preference is set or the form definition requests auto-deletion.
        // TODO: this could take some time so might be better to do in a separate process,
        // perhaps another worker. It also feels like this could fail and if so should be
        // communicated to the user. Maybe successful delete should also be communicated?
        if (InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, generalSettings.getBoolean(ProjectKeys.KEY_DELETE_AFTER_SEND), instance)) {
            InstanceDeleter(
                InstancesRepositoryProvider(org.espen.collect.android.application.EspenCollect.getInstance()).get(),
                FormsRepositoryProvider(org.espen.collect.android.application.EspenCollect.getInstance()).get()
            ).delete(instance.dbId)
        }
    }

    private fun logUploadedForm(instance: Instance) {
        val key = if (isGoogleSheetsProtocol()) "HTTP-Sheets auto" else "HTTP auto"
        val value = org.espen.collect.android.application.EspenCollect.getFormIdentifierHash(instance.formId, instance.formVersion)

        Analytics.log(AnalyticsEvents.SUBMISSION, key, value)
    }
}
