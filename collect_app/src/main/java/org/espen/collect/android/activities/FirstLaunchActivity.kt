package org.espen.collect.android.activities

import android.os.Bundle
import org.odk.collect.analytics.Analytics
import org.espen.collect.android.analytics.AnalyticsEvents
import org.espen.collect.android.databinding.FirstLaunchLayoutBinding
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.mainmenu.MainMenuActivity
import org.espen.collect.android.projects.ManualProjectCreatorDialog
import org.espen.collect.android.projects.ProjectsDataService
import org.espen.collect.android.projects.QrCodeProjectCreatorDialog
import org.espen.collect.android.version.VersionInformation
import org.espen.collect.androidshared.ui.DialogFragmentUtils
import org.espen.collect.androidshared.ui.GroupClickListener.addOnClickListener
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class FirstLaunchActivity : LocalizedActivity() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var versionInformation: org.espen.collect.android.version.VersionInformation

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var settingsProvider: SettingsProvider

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.espen.collect.android.injection.DaggerUtils.getComponent(this).inject(this)

        FirstLaunchLayoutBinding.inflate(layoutInflater).apply {
            setContentView(this.root)

            configureViaQrButton.setOnClickListener {
                DialogFragmentUtils.showIfNotShowing(
                    QrCodeProjectCreatorDialog::class.java,
                    supportFragmentManager
                )
            }

            configureManuallyButton.setOnClickListener {
                DialogFragmentUtils.showIfNotShowing(
                    ManualProjectCreatorDialog::class.java,
                    supportFragmentManager
                )
            }

            appName.text = String.format(
                "%s %s",
                getString(org.odk.collect.strings.R.string.collect_app_name),
                versionInformation.versionToDisplay
            )

            configureLater.addOnClickListener {
                Analytics.log(AnalyticsEvents.TRY_DEMO)

                projectsRepository.save(Project.DEMO_PROJECT)
                projectsDataService.setCurrentProject(Project.DEMO_PROJECT_ID)

                org.espen.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                    this@FirstLaunchActivity,
                    MainMenuActivity::class.java
                )
            }
        }
    }
}
