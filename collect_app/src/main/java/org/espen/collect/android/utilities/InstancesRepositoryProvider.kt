package org.espen.collect.android.utilities

import android.content.Context
import org.espen.collect.android.database.instances.DatabaseInstancesRepository
import org.espen.collect.android.storage.StoragePathProvider
import org.espen.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.instances.InstancesRepository

class InstancesRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider = StoragePathProvider()
) {

    @JvmOverloads
    fun get(projectId: String? = null): InstancesRepository {
        return org.espen.collect.android.database.instances.DatabaseInstancesRepository(
                context,
                storagePathProvider.getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.METADATA, projectId),
                storagePathProvider.getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.INSTANCES, projectId),
                System::currentTimeMillis
        )
    }
}
