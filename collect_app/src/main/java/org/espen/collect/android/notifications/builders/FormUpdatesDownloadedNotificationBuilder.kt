package org.espen.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.espen.collect.android.formlists.blankformlist.BlankFormListActivity
import org.espen.collect.android.formmanagement.FormDownloadException
import org.espen.collect.android.formmanagement.ServerFormDetails
import org.espen.collect.android.notifications.NotificationManagerNotifier
import org.espen.collect.android.utilities.ApplicationConstants
import org.espen.collect.android.utilities.FormsDownloadResultInterpreter
import org.odk.collect.errors.ErrorActivity
import org.odk.collect.strings.localization.getLocalizedString
import java.io.Serializable

object FormUpdatesDownloadedNotificationBuilder {

    fun build(application: Application, result: Map<ServerFormDetails, FormDownloadException?>, projectName: String): Notification {
        val allFormsDownloadedSuccessfully = FormsDownloadResultInterpreter.allFormsDownloadedSuccessfully(result)

        val intent = if (allFormsDownloadedSuccessfully) {
            Intent(application, BlankFormListActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        } else {
            Intent(application, ErrorActivity::class.java).apply {
                putExtra(ErrorActivity.EXTRA_ERRORS, FormsDownloadResultInterpreter.getFailures(result, application) as Serializable)
            }
        }

        val contentIntent = PendingIntent.getActivity(
            application,
            org.espen.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_DOWNLOADED_NOTIFICATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title =
            if (allFormsDownloadedSuccessfully) {
                application.getLocalizedString(org.odk.collect.strings.R.string.forms_download_succeeded)
            } else {
                application.getLocalizedString(org.odk.collect.strings.R.string.forms_download_failed)
            }

        val message =
            if (allFormsDownloadedSuccessfully) {
                application.getLocalizedString(org.odk.collect.strings.R.string.all_downloads_succeeded)
            } else {
                application.getLocalizedString(
                    org.odk.collect.strings.R.string.some_downloads_failed,
                    FormsDownloadResultInterpreter.getNumberOfFailures(result),
                    result.size
                )
            }

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(title)
            setContentText(message)
            setSubText(projectName)
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)
        }.build()
    }
}
