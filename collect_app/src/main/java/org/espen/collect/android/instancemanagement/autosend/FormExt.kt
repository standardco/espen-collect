package org.espen.collect.android.instancemanagement.autosend

import org.odk.collect.forms.Form

fun Form.shouldFormBeSentAutomatically(isAutoSendEnabledInSettings: Boolean): Boolean {
    return if (isAutoSendEnabledInSettings) {
        getAutoSendMode() != FormAutoSendMode.OPT_OUT
    } else {
        getAutoSendMode() == FormAutoSendMode.FORCED
    }
}

fun Form.getAutoSendMode(): FormAutoSendMode {
    return if (autoSend?.trim()?.lowercase() == "false") {
        FormAutoSendMode.OPT_OUT
    } else if (autoSend?.trim()?.lowercase() == "true") {
        FormAutoSendMode.FORCED
    } else {
        FormAutoSendMode.NEUTRAL
    }
}

enum class FormAutoSendMode {
    OPT_OUT,
    FORCED,
    NEUTRAL
}
