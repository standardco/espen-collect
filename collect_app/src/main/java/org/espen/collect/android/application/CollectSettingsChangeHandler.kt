package org.espen.collect.android.application

import org.espen.collect.android.analytics.AnalyticsUtils
import org.espen.collect.android.backgroundwork.FormUpdateScheduler
import org.espen.collect.android.formmanagement.FormsDataService
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.settings.importing.SettingsChangeHandler
import org.odk.collect.settings.keys.ProjectKeys

class CollectSettingsChangeHandler(
    private val propertyManager: PropertyManager,
    private val formUpdateScheduler: FormUpdateScheduler,
    private val formsDataService: FormsDataService
) : SettingsChangeHandler {

    override fun onSettingChanged(projectId: String, newValue: Any?, changedKey: String) {
        propertyManager.reload()

        if (changedKey == ProjectKeys.KEY_SERVER_URL) {
            formsDataService.clear(projectId)
        }

        if (changedKey == ProjectKeys.KEY_FORM_UPDATE_MODE ||
            changedKey == ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK
        ) {
            formUpdateScheduler.scheduleUpdates(projectId)
        }

        if (changedKey == ProjectKeys.KEY_SERVER_URL) {
            AnalyticsUtils.logServerConfiguration(newValue.toString())
        }
    }

    override fun onSettingsChanged(projectId: String) {
        propertyManager.reload()
        formUpdateScheduler.scheduleUpdates(projectId)
    }
}
