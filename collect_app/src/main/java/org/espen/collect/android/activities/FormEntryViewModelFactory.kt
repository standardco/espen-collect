package org.espen.collect.android.activities

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import org.javarosa.core.model.actions.recordaudio.RecordAudioActions
import org.javarosa.core.model.instance.TreeReference
import org.espen.collect.android.entities.EntitiesRepositoryProvider
import org.espen.collect.android.formentry.BackgroundAudioViewModel
import org.espen.collect.android.formentry.BackgroundAudioViewModel.RecordAudioActionRegistry
import org.espen.collect.android.formentry.FormEndViewModel
import org.espen.collect.android.formentry.FormEntryViewModel
import org.espen.collect.android.formentry.FormSessionRepository
import org.espen.collect.android.formentry.audit.IdentityPromptViewModel
import org.espen.collect.android.formentry.backgroundlocation.BackgroundLocationHelper
import org.espen.collect.android.formentry.backgroundlocation.BackgroundLocationManager
import org.espen.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel
import org.espen.collect.android.formentry.saving.DiskFormSaver
import org.espen.collect.android.formentry.saving.FormSaveViewModel
import org.espen.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.espen.collect.android.projects.ProjectsDataService
import org.espen.collect.android.utilities.ApplicationConstants
import org.espen.collect.android.utilities.InstancesRepositoryProvider
import org.espen.collect.android.utilities.LookUpRepositoryProvider
import org.espen.collect.android.utilities.MediaUtils
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.location.LocationClient
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.SettingsProvider
import java.util.function.BiConsumer

class FormEntryViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val mode: String?,
    private val sessionId: String,
    private val scheduler: Scheduler,
    private val formSessionRepository: FormSessionRepository,
    private val mediaUtils: MediaUtils,
    private val audioRecorder: AudioRecorder,
    private val projectsDataService: ProjectsDataService,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider,
    private val settingsProvider: SettingsProvider,
    private val permissionsChecker: PermissionsChecker,
    private val fusedLocationClient: LocationClient,
    private val permissionsProvider: PermissionsProvider,
    private val autoSendSettingsProvider: AutoSendSettingsProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val lookupRepositoryProvider: LookUpRepositoryProvider,

) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        val projectId = projectsDataService.getCurrentProject().uuid

        return when (modelClass) {
            org.espen.collect.android.formentry.FormEntryViewModel::class.java -> org.espen.collect.android.formentry.FormEntryViewModel(
                    System::currentTimeMillis,
                    scheduler,
                    formSessionRepository,
                    lookupRepositoryProvider.get(projectId),
                    sessionId
            )

            org.espen.collect.android.formentry.saving.FormSaveViewModel::class.java -> {
                org.espen.collect.android.formentry.saving.FormSaveViewModel(
                        handle,
                        System::currentTimeMillis,
                        org.espen.collect.android.formentry.saving.DiskFormSaver(),
                        mediaUtils,
                        scheduler,
                        audioRecorder,
                        projectsDataService,
                        formSessionRepository.get(sessionId),
                        entitiesRepositoryProvider.get(projectId),
                        instancesRepositoryProvider.get(projectId),
                        lookupRepositoryProvider.get(projectId)
                )
            }

            org.espen.collect.android.formentry.BackgroundAudioViewModel::class.java -> {
                val recordAudioActionRegistry =
                    if (mode == org.espen.collect.android.utilities.ApplicationConstants.FormModes.VIEW_SENT) {
                        object : RecordAudioActionRegistry {
                            override fun register(listener: BiConsumer<TreeReference, String?>) {}
                            override fun unregister() {}
                        }
                    } else {
                        object : RecordAudioActionRegistry {
                            override fun register(listener: BiConsumer<TreeReference, String?>) {
                                RecordAudioActions.setRecordAudioListener { absoluteTargetRef: TreeReference, quality: String? ->
                                    listener.accept(absoluteTargetRef, quality)
                                }
                            }

                            override fun unregister() {
                                RecordAudioActions.setRecordAudioListener(null)
                            }
                        }
                    }

                org.espen.collect.android.formentry.BackgroundAudioViewModel(
                        audioRecorder,
                        settingsProvider.getUnprotectedSettings(),
                        recordAudioActionRegistry,
                        permissionsChecker,
                        System::currentTimeMillis,
                        formSessionRepository.get(sessionId)
                )
            }

            org.espen.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel::class.java -> {
                val locationManager = org.espen.collect.android.formentry.backgroundlocation.BackgroundLocationManager(
                        fusedLocationClient,
                        org.espen.collect.android.formentry.backgroundlocation.BackgroundLocationHelper(
                                permissionsProvider,
                                settingsProvider.getUnprotectedSettings()
                        ) {
                            formSessionRepository.get(sessionId).value?.formController
                        }
                )

                org.espen.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel(locationManager)
            }

            org.espen.collect.android.formentry.audit.IdentityPromptViewModel::class.java -> org.espen.collect.android.formentry.audit.IdentityPromptViewModel()

            FormEndViewModel::class.java -> FormEndViewModel(
                formSessionRepository,
                sessionId,
                settingsProvider,
                autoSendSettingsProvider
            )

            else -> throw IllegalArgumentException()
        } as T
    }
}
