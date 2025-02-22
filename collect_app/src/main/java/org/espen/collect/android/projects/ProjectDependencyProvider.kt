package org.espen.collect.android.projects

import org.espen.collect.android.formmanagement.FormSourceProvider
import org.espen.collect.android.storage.StoragePathProvider
import org.espen.collect.android.storage.StorageSubdirectory
import org.espen.collect.android.utilities.ChangeLockProvider
import org.espen.collect.android.utilities.FormsRepositoryProvider
import org.espen.collect.android.utilities.InstancesRepositoryProvider
import org.espen.collect.android.utilities.LookUpRepositoryProvider
import org.odk.collect.settings.SettingsProvider

/**
 * Provides all the basic/building block dependencies needed when performing logic inside a
 * project.
 */
data class ProjectDependencyProvider(
    val projectId: String,
    val settingsProvider: SettingsProvider,
    val formsRepositoryProvider: FormsRepositoryProvider,
    val instancesRepositoryProvider: InstancesRepositoryProvider,
    val lookupRepositoryProvider: LookUpRepositoryProvider,
    val storagePathProvider: StoragePathProvider,
    val changeLockProvider: ChangeLockProvider,
    val formSourceProvider: FormSourceProvider
) {
    val generalSettings by lazy { settingsProvider.getUnprotectedSettings(projectId) }
    val formsRepository by lazy { formsRepositoryProvider.get(projectId) }
    val instancesRepository by lazy { instancesRepositoryProvider.get(projectId) }
    val lookupRepository by lazy { lookupRepositoryProvider.get(projectId) }
    val formSource by lazy { formSourceProvider.get(projectId) }
    val formsLock by lazy { changeLockProvider.getFormLock(projectId) }
    val formsDir by lazy { storagePathProvider.getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.FORMS, projectId) }
    val cacheDir by lazy { storagePathProvider.getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.CACHE, projectId) }
}

class ProjectDependencyProviderFactory(
    private val settingsProvider: SettingsProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val lookupRepositoryProvider: LookUpRepositoryProvider,
    private val storagePathProvider: StoragePathProvider,
    private val changeLockProvider: ChangeLockProvider,
    private val formSourceProvider: FormSourceProvider
) {
    fun create(projectId: String) = ProjectDependencyProvider(
        projectId,
        settingsProvider,
        formsRepositoryProvider,
        instancesRepositoryProvider, lookupRepositoryProvider,
        storagePathProvider,
        changeLockProvider,
        formSourceProvider
    )
}
