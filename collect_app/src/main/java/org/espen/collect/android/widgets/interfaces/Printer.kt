package org.espen.collect.android.widgets.interfaces

import android.content.Context
import org.espen.collect.android.utilities.QuestionMediaManager
import org.odk.collect.androidshared.livedata.NonNullLiveData

interface Printer {
    fun parseAndPrint(
        htmlDocument: String,
        questionMediaManager: QuestionMediaManager,
        context: Context
    )

    fun isLoading(): NonNullLiveData<Boolean>
}
