package org.espen.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.espen.collect.android.R
import org.espen.collect.android.instancemanagement.send.InstanceUploaderListActivity
import org.espen.collect.android.mainmenu.MainMenuActivity
import org.espen.collect.android.notifications.NotificationManagerNotifier
import org.espen.collect.android.upload.FormUploadException
import org.espen.collect.android.utilities.ApplicationConstants.RequestCodes
import org.espen.collect.android.utilities.FormsUploadResultInterpreter
import org.odk.collect.errors.ErrorActivity
import org.odk.collect.forms.instances.Instance
import org.odk.collect.strings.localization.getLocalizedString
import java.io.Serializable

object FormsSubmissionNotificationBuilder {

    fun build(application: Application, result: Map<Instance, FormUploadException?>, projectName: String): Notification {
        val allFormsUploadedSuccessfully = FormsUploadResultInterpreter.allFormsUploadedSuccessfully(result)

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(getNotificationPendingIntent(application, allFormsUploadedSuccessfully))
            setContentTitle(getTitle(application, allFormsUploadedSuccessfully))
            setContentText(getMessage(application, allFormsUploadedSuccessfully, result))
            setSubText(projectName)
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)

            if (!allFormsUploadedSuccessfully) {
                addAction(
                    R.drawable.ic_outline_info_small,
                    application.getLocalizedString(org.odk.collect.strings.R.string.show_details),
                    getShowDetailsPendingIntent(application, result)
                )
            }
        }.build()
    }

    private fun getTitle(application: Application, allFormsUploadedSuccessfully: Boolean): String {
        return if (allFormsUploadedSuccessfully) {
            application.getLocalizedString(org.odk.collect.strings.R.string.forms_upload_succeeded)
        } else {
            application.getLocalizedString(org.odk.collect.strings.R.string.forms_upload_failed)
        }
    }

    private fun getMessage(application: Application, allFormsUploadedSuccessfully: Boolean, result: Map<Instance, FormUploadException?>): String {
        return if (allFormsUploadedSuccessfully) {
            application.getLocalizedString(org.odk.collect.strings.R.string.all_uploads_succeeded)
        } else {
            application.getLocalizedString(
                org.odk.collect.strings.R.string.some_uploads_failed,
                FormsUploadResultInterpreter.getNumberOfFailures(result),
                result.size
            )
        }
    }

    private fun getNotificationPendingIntent(application: Application, allFormsUploadedSuccessfully: Boolean): PendingIntent {
        val notifyIntent = if (allFormsUploadedSuccessfully) {
            Intent(application, MainMenuActivity::class.java)
        } else {
            Intent(application, org.espen.collect.android.instancemanagement.send.InstanceUploaderListActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return PendingIntent.getActivity(
            application,
            RequestCodes.FORMS_UPLOADED_NOTIFICATION,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getShowDetailsPendingIntent(application: Application, result: Map<Instance, FormUploadException?>): PendingIntent {
        val showDetailsIntent = Intent(application, ErrorActivity::class.java).apply {
            putExtra(ErrorActivity.EXTRA_ERRORS, FormsUploadResultInterpreter.getFailures(result, application) as Serializable)
        }

        return PendingIntent.getActivity(
            application,
            RequestCodes.FORMS_UPLOADED_NOTIFICATION,
            showDetailsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
