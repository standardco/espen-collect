package org.espen.collect.android.storage

import org.espen.collect.android.application.EspenCollect
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.projects.ProjectsDataService
import org.espen.collect.android.utilities.FileUtils
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.PathUtils
import timber.log.Timber
import java.io.File

class StoragePathProvider(
        private val projectsDataService: ProjectsDataService = org.espen.collect.android.injection.DaggerUtils.getComponent(org.espen.collect.android.application.EspenCollect.getInstance()).currentProjectProvider(),
        private val projectsRepository: ProjectsRepository = org.espen.collect.android.injection.DaggerUtils.getComponent(org.espen.collect.android.application.EspenCollect.getInstance()).projectsRepository(),
        val odkRootDirPath: String = org.espen.collect.android.application.EspenCollect.getInstance().getExternalFilesDir(null)!!.absolutePath
) {

    @JvmOverloads
    fun getProjectRootDirPath(projectId: String? = null): String {
        val uuid = projectId ?: projectsDataService.getCurrentProject().uuid
        val path = getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.PROJECTS) + File.separator + uuid

        if (!File(path).exists()) {
            File(path).mkdirs()

            try {
                val sanitizedProjectName = PathUtils.getPathSafeFileName(projectsRepository.get(uuid)!!.name)
                File(path + File.separator + sanitizedProjectName).createNewFile()
            } catch (e: Exception) {
                Timber.e(
                    Error(
                        org.espen.collect.android.utilities.FileUtils.getFilenameError(
                            projectsRepository.get(uuid)!!.name
                        )
                    )
                )
            }
        }

        return path
    }

    @JvmOverloads
    fun getOdkDirPath(subdirectory: org.espen.collect.android.storage.StorageSubdirectory, projectId: String? = null): String {
        val path = when (subdirectory) {
            org.espen.collect.android.storage.StorageSubdirectory.PROJECTS,
            org.espen.collect.android.storage.StorageSubdirectory.SHARED_LAYERS -> odkRootDirPath + File.separator + subdirectory.directoryName
            org.espen.collect.android.storage.StorageSubdirectory.FORMS,
            org.espen.collect.android.storage.StorageSubdirectory.INSTANCES,
            org.espen.collect.android.storage.StorageSubdirectory.LOOKUPS,
            org.espen.collect.android.storage.StorageSubdirectory.CACHE,
            org.espen.collect.android.storage.StorageSubdirectory.METADATA,
            org.espen.collect.android.storage.StorageSubdirectory.LAYERS,
            org.espen.collect.android.storage.StorageSubdirectory.SETTINGS -> getProjectRootDirPath(projectId) + File.separator + subdirectory.directoryName
        }

        if (!File(path).exists()) {
            File(path).mkdirs()
        }

        return path
    }

    @Deprecated(
        message = "Should use specific temp file or create a new file in StorageSubdirectory.CACHE instead",
        ReplaceWith(
            "getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + \"tmp.jpg\""
        )
    )
    fun getTmpImageFilePath(): String {
        return getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.CACHE) + File.separator + "tmp.jpg"
    }

    @Deprecated(
        message = "Should use specific temp file or create a new file in StorageSubdirectory.CACHE instead",
        ReplaceWith(
            "getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + \"tmp.mp4\""
        )
    )
    fun getTmpVideoFilePath(): String {
        return getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.CACHE) + File.separator + "tmp.mp4"
    }
}
