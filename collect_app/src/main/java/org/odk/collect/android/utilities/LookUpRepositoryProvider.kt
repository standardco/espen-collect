package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.android.database.lookups.DatabaseLookupRepository
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.lookup.LookUpRepository

class LookUpRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider = StoragePathProvider()
) {

    @JvmOverloads
    fun get(projectId: String? = null): LookUpRepository {

        return DatabaseLookupRepository(
            context,
            storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, projectId),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.LOOKUPS, projectId),
            System::currentTimeMillis
        )
    }
}
