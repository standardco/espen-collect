package org.espen.collect.android.mainmenu

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.espen.collect.android.application.initialization.AnalyticsInitializer
import org.espen.collect.android.formmanagement.InstancesAppState
import org.espen.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.espen.collect.android.projects.ProjectsDataService
import org.espen.collect.android.utilities.FormsRepositoryProvider
import org.espen.collect.android.utilities.InstancesRepositoryProvider
import org.espen.collect.android.version.VersionInformation
import org.odk.collect.async.Scheduler
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.settings.SettingsProvider

open class MainMenuViewModelFactory(
        private val versionInformation: org.espen.collect.android.version.VersionInformation,
        private val application: Application,
        private val settingsProvider: SettingsProvider,
        private val instancesAppState: InstancesAppState,
        private val scheduler: Scheduler,
        private val projectsDataService: ProjectsDataService,
        private val analyticsInitializer: AnalyticsInitializer,
        private val permissionChecker: PermissionsChecker,
        private val formsRepositoryProvider: FormsRepositoryProvider,
        private val instancesRepositoryProvider: InstancesRepositoryProvider,
        private val autoSendSettingsProvider: AutoSendSettingsProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            MainMenuViewModel::class.java -> MainMenuViewModel(
                application,
                versionInformation,
                settingsProvider,
                instancesAppState,
                scheduler,
                formsRepositoryProvider,
                instancesRepositoryProvider,
                autoSendSettingsProvider
            )

            CurrentProjectViewModel::class.java -> CurrentProjectViewModel(
                projectsDataService
            )

            RequestPermissionsViewModel::class.java -> RequestPermissionsViewModel(
                settingsProvider,
                permissionChecker
            )

            else -> throw IllegalArgumentException()
        } as T
    }
}
