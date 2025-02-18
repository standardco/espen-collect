package org.espen.collect.android.utilities

import android.content.Context
import org.espen.collect.android.application.Collect
import org.espen.collect.android.database.instances.DatabaseInstancesRepository
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.storage.StoragePathProvider
import org.espen.collect.android.storage.StoragePaths
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.projects.ProjectDependencyFactory

class InstancesRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths> = StoragePathProvider()
) : ProjectDependencyFactory<InstancesRepository> {

    override fun create(projectId: String): InstancesRepository {
        val storagePaths = storagePathFactory.create(projectId)
        return DatabaseInstancesRepository(
            context,
            storagePaths.metaDir,
            storagePaths.instancesDir,
            System::currentTimeMillis
        )
    }

    @Deprecated("Creating dependency without specified project is dangerous")
    fun create(): InstancesRepository {
        val currentProject =
            DaggerUtils.getComponent(Collect.getInstance()).currentProjectProvider()
                .getCurrentProject()
        return create(currentProject.uuid)
    }
}
