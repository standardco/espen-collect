package org.espen.collect.android.utilities

import android.content.Context
import org.espen.collect.android.application.Collect
import org.espen.collect.android.database.instances.DatabaseInstancesRepository
import org.espen.collect.android.database.lookups.DatabaseLookupRepository
import org.espen.collect.android.injection.DaggerUtils
//import org.espen.collect.android.storage.StoragePaths
import org.espen.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.lookup.LookUpRepository
import org.odk.collect.projects.ProjectDependencyFactory
import org.espen.collect.android.storage.StoragePathProvider
import org.espen.collect.android.storage.StoragePaths

class LookUpRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths> = StoragePathProvider()
) : ProjectDependencyFactory<LookUpRepository> {

    override fun create(projectId: String): LookUpRepository {
        val storagePaths = storagePathFactory.create(projectId)
        return DatabaseLookupRepository(
            context,
            storagePaths.metaDir,
            storagePaths.lookupsDir,
            System::currentTimeMillis
        )
    }

    @Deprecated("Creating dependency without specified project is dangerous")
    fun create(): LookUpRepository {
        val currentProject =
            DaggerUtils.getComponent(Collect.getInstance()).currentProjectProvider()
                .getCurrentProject()
        return create(currentProject.uuid)
    }
    //@JvmOverloads
    fun get(projectId: String): LookUpRepository {
        val storagePaths = storagePathFactory.create(projectId)
        return DatabaseLookupRepository(
            context,
            storagePaths.metaDir,
            storagePaths.lookupsDir,
            System::currentTimeMillis
        )
    }
}
