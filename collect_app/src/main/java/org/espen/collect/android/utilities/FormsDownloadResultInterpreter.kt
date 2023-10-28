package org.espen.collect.android.utilities

import android.content.Context
import org.espen.collect.android.formmanagement.FormDownloadException
import org.espen.collect.android.formmanagement.FormDownloadExceptionMapper
import org.espen.collect.android.formmanagement.ServerFormDetails
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
