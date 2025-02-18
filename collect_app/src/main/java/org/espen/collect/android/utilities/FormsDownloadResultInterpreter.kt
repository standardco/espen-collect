package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.formmanagement.download.FormDownloadException
import org.odk.collect.android.formmanagement.download.FormDownloadExceptionMapper
import org.odk.collect.errors.ErrorItem
import org.odk.collect.strings.localization.getLocalizedString

object FormsDownloadResultInterpreter {
    fun getFailures(result: Map<ServerFormDetails, FormDownloadException?>, context: Context) = result.filter {
        it.value != null
    }.map {
        ErrorItem(
            it.key.formName ?: "",
            context.getLocalizedString(org.odk.collect.strings.R.string.form_details, it.key.formId ?: "", it.key.formVersion ?: ""),
            FormDownloadExceptionMapper(context).getMessage(it.value)
        )
    }

    fun getNumberOfFailures(result: Map<ServerFormDetails, FormDownloadException?>) = result.count {
        it.value != null
    }

    fun allFormsDownloadedSuccessfully(result: Map<ServerFormDetails, FormDownloadException?>) = result.values.all {
        it == null
    }
}
