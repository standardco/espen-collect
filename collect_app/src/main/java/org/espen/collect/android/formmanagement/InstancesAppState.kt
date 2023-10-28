package org.espen.collect.android.formmanagement

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.espen.collect.android.external.InstancesContract
import org.espen.collect.android.projects.ProjectsDataService
import org.espen.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import javax.inject.Singleton

/**
 * Stores reactive state of instances. This (as a singleton) can be read or updated by
 * different parts of the app without needing reactive data in the [InstancesRepository].
 */
@Singleton
class InstancesAppState(
    private val context: Context,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val projectsDataService: ProjectsDataService
) {

    private val _editable = MutableLiveData(0)
    val editableCount: LiveData<Int> = _editable

    private val _sendable = MutableLiveData(0)
    val sendableCount: LiveData<Int> = _sendable

    private val _sent = MutableLiveData(0)
    val sentCount: LiveData<Int> = _sent

    fun update() {
        val instancesRepository = instancesRepositoryProvider.get()

        val sendableInstances = instancesRepository.getCountByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )
        val sentInstances = instancesRepository.getCountByStatus(Instance.STATUS_SUBMITTED, Instance.STATUS_SUBMISSION_FAILED)
        val editableInstances = instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE)

        _sendable.postValue(sendableInstances)
        _sent.postValue(sentInstances)
        _editable.postValue(editableInstances)

        context.contentResolver.notifyChange(
            org.espen.collect.android.external.InstancesContract.getUri(projectsDataService.getCurrentProject().uuid),
            null
        )
    }
}
