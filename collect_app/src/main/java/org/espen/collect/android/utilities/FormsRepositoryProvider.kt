package org.espen.collect.android.utilities

import android.content.Context
import org.espen.collect.android.database.forms.DatabaseFormsRepository
import org.espen.collect.android.storage.StoragePathProvider
import org.espen.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.FormsRepository

class FormsRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider = StoragePathProvider()
) {

    private val clock = { System.currentTimeMillis() }

    @JvmOverloads
    fun get(projectId: String? = null): FormsRepository {
        val dbPath = storagePathProvider.getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.METADATA, projectId)
        val formsPath = storagePathProvider.getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.FORMS, projectId)
        val cachePath = storagePathProvider.getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.CACHE, projectId)
        return org.espen.collect.android.database.forms.DatabaseFormsRepository(context, dbPath, formsPath, cachePath, clock)
    }
}
