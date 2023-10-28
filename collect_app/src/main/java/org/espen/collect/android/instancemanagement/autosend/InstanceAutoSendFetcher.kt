package org.espen.collect.android.instancemanagement.autosend

import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository

class InstanceAutoSendFetcher(private val autoSendSettingsProvider: AutoSendSettingsProvider) {

    fun getInstancesToAutoSend(projectId: String, instancesRepository: InstancesRepository, formsRepository: FormsRepository): List<Instance> {
        val allFinalizedForms = instancesRepository.getAllByStatus(Instance.STATUS_COMPLETE, Instance.STATUS_SUBMISSION_FAILED)

        val isAutoSendEnabledInSettings = autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId)
        return allFinalizedForms.filter {
            formsRepository.getLatestByFormIdAndVersion(it.formId, it.formVersion)?.let { form ->
                form.shouldFormBeSentAutomatically(isAutoSendEnabledInSettings)
            } ?: false
        }
    }
}
