package org.espen.collect.android.application.initialization.upgrade

import android.content.Context
import org.espen.collect.android.BuildConfig
import org.espen.collect.android.application.initialization.ExistingProjectMigrator
import org.espen.collect.android.application.initialization.ExistingSettingsMigrator
import org.espen.collect.android.application.initialization.FormUpdatesUpgrade
import org.espen.collect.android.application.initialization.GoogleDriveProjectsDeleter
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.upgrade.AppUpgrader

class UpgradeInitializer(
    private val context: Context,
    private val settingsProvider: SettingsProvider,
    private val existingProjectMigrator: ExistingProjectMigrator,
    private val existingSettingsMigrator: ExistingSettingsMigrator,
    private val formUpdatesUpgrade: FormUpdatesUpgrade,
    private val googleDriveProjectsDeleter: GoogleDriveProjectsDeleter
) {

    fun initialize() {
        AppUpgrader(
            MetaKeys.LAST_LAUNCHED,
            settingsProvider.getMetaSettings(),
            BuildConfig.VERSION_CODE,
            BeforeProjectsInstallDetector(context),
            listOf(
                existingProjectMigrator,
                existingSettingsMigrator,
                formUpdatesUpgrade,
                googleDriveProjectsDeleter
            )
        ).upgradeIfNeeded()
    }
}
