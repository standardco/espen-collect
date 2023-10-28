package org.espen.collect.android.widgets.interfaces

import org.javarosa.form.api.FormEntryPrompt
import org.espen.collect.android.widgets.utilities.WaitingForDataRegistry

interface GeoDataRequester {
    fun requestGeoPoint(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: org.espen.collect.android.widgets.utilities.WaitingForDataRegistry
    )

    fun requestGeoShape(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: org.espen.collect.android.widgets.utilities.WaitingForDataRegistry
    )

    fun requestGeoTrace(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: org.espen.collect.android.widgets.utilities.WaitingForDataRegistry
    )
}
