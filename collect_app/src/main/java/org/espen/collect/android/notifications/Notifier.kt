package org.espen.collect.android.notifications

import org.espen.collect.android.formmanagement.ServerFormDetails
import org.espen.collect.android.formmanagement.download.FormDownloadException
import org.espen.collect.android.upload.FormUploadException
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.instances.Instance

interface Notifier {
    fun onUpdatesAvailable(updates: List<ServerFormDetails>, projectId: String)
    fun onUpdatesDownloaded(result: Map<ServerFormDetails, FormDownloadException?>, projectId: String)
    fun onSync(exception: FormSourceException?, projectId: String)
    fun onSubmission(result: Map<Instance, FormUploadException?>, projectId: String)
}
