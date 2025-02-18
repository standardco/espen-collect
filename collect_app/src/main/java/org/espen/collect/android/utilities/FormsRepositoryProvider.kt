package org.espen.collect.android.utilities

import android.content.Context
import org.espen.collect.android.application.Collect
import org.espen.collect.android.database.forms.DatabaseFormsRepository
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.storage.StoragePathProvider
import org.espen.collect.android.storage.StoragePaths
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.savepoints.SavepointsRepository
import org.odk.collect.projects.ProjectDependencyFactory

class FormsRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths> = StoragePathProvider(),
    private val savepointsRepositoryProvider: ProjectDependencyFactory<SavepointsRepository> = SavepointsRepositoryProvider(
        context,
        storagePathFactory
    )
) : ProjectDependencyFactory<FormsRepository> {

    private val clock = { System.currentTimeMillis() }

    override fun create(projectId: String): FormsRepository {
        val storagePaths = storagePathFactory.create(projectId)
        return DatabaseFormsRepository(
            context,
            storagePaths.metaDir,
            storagePaths.formsDir,
            storagePaths.cacheDir,
            clock,
            savepointsRepositoryProvider.create(projectId)
        )
    }

    @Deprecated("Creating dependency without specified project is dangerous")
    fun create(): FormsRepository {
        val currentProject =
            DaggerUtils.getComponent(Collect.getInstance()).currentProjectProvider()
                .getCurrentProject()
        return create(currentProject.uuid)
    }
}
