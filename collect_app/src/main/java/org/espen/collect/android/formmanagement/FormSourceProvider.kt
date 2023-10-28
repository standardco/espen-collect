package org.espen.collect.android.formmanagement

import org.espen.collect.android.openrosa.OpenRosaFormSource
import org.espen.collect.android.openrosa.OpenRosaHttpInterface
import org.espen.collect.android.openrosa.OpenRosaResponseParserImpl
import org.espen.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.FormSource
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class FormSourceProvider(
    private val settingsProvider: SettingsProvider,
    private val openRosaHttpInterface: org.espen.collect.android.openrosa.OpenRosaHttpInterface
) {

    @JvmOverloads
    fun get(projectId: String? = null): FormSource {
        val generalSettings = settingsProvider.getUnprotectedSettings(projectId)

        val serverURL = generalSettings.getString(ProjectKeys.KEY_SERVER_URL)

        return org.espen.collect.android.openrosa.OpenRosaFormSource(
                serverURL,
                openRosaHttpInterface,
                org.espen.collect.android.utilities.WebCredentialsUtils(generalSettings),
                OpenRosaResponseParserImpl()
        )
    }
}
