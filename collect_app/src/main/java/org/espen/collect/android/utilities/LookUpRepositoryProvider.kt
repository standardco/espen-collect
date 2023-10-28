package org.espen.collect.android.utilities

import android.content.Context
import org.espen.collect.android.database.lookups.DatabaseLookupRepository
import org.espen.collect.android.storage.StoragePathProvider
import org.espen.collect.android.storage.StorageSubdirectory
import org.odk.collect.lookup.LookUpRepository

class LookUpRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider = StoragePathProvider()
) {

    @JvmOverloads
    fun get(projectId: String? = null): LookUpRepository {

        return org.espen.collect.android.database.lookups.DatabaseLookupRepository(
                context,
                storagePathProvider.getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.METADATA, projectId),
                storagePathProvider.getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.LOOKUPS, projectId),
                System::currentTimeMillis
        )
    }
}
