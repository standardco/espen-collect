package org.odk.collect.android.application.initialization

import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.ODKAppSettingsMigrator
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.upgrade.Upgrade

class ExistingSettingsMigrator(
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val settingsMigrator: ODKAppSettingsMigrator
) : Upgrade {

    override fun key(): String? {
        return null
    }

    override fun run() {
        projectsRepository.getAll().forEach {
            settingsMigrator.migrate(
                settingsProvider.getUnprotectedSettings(it.uuid),
                settingsProvider.getProtectedSettings(it.uuid)
            )
        }
    }
}
