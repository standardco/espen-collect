package org.espen.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.espen.collect.android.activities.FormDownloadListActivity
import org.espen.collect.android.notifications.NotificationManagerNotifier
import org.espen.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.strings.localization.getLocalizedString

object FormUpdatesAvailableNotificationBuilder {

    @JvmStatic
    fun build(application: Application, projectName: String): Notification {
        val intent = Intent(application, org.espen.collect.android.activities.FormDownloadListActivity::class.java).apply {
            putExtra(org.espen.collect.android.activities.FormDownloadListActivity.DISPLAY_ONLY_UPDATED_FORMS, true)
        }

        val contentIntent = PendingIntent.getActivity(
            application,
            RequestCodes.FORM_UPDATES_AVAILABLE_NOTIFICATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(org.odk.collect.strings.R.string.form_updates_available))
            setContentText(null)
            setSubText(projectName)
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)
        }.build()
    }
}
