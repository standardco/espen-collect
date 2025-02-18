package org.espen.collect.android.mainmenu

import android.os.Build
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import org.espen.collect.android.R
import org.espen.collect.android.activities.ActivityUtils
import org.espen.collect.android.activities.CrashHandlerActivity
import org.espen.collect.android.activities.FirstLaunchActivity
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.projects.ProjectSettingsDialog
import org.espen.collect.android.utilities.ThemeUtils
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.crashhandler.CrashHandler
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class MainMenuActivity : LocalizedActivity() {

    @Inject
    lateinit var viewModelFactory: MainMenuViewModelFactory

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        initSplashScreen()

        /*
        Don't reopen if the app is already open - allows entry points like notifications to use
        this Activity as a target to reopen the app without interrupting an ongoing session
         */
        if (!isTaskRoot) {
            super.onCreate(null)
            finish()
            return
        }

        CrashHandler.getInstance(this)?.also {
            if (it.hasCrashed(this)) {
                super.onCreate(null)
                ActivityUtils.startActivityAndCloseAllOthers(this, CrashHandlerActivity::class.java)
                return
            }
        }

        DaggerUtils.getComponent(this).inject(this)

        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        currentProjectViewModel = viewModelProvider[CurrentProjectViewModel::class.java]

        ThemeUtils(this).setDarkModeForCurrentProject()

        if (!currentProjectViewModel.hasCurrentProject()) {
            super.onCreate(null)
            ActivityUtils.startActivityAndCloseAllOthers(this, FirstLaunchActivity::class.java)
            return
        } else {
            this.supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
                .forClass(PermissionsDialogFragment::class) {
                    PermissionsDialogFragment(
                        permissionsProvider,
                        viewModelProvider[RequestPermissionsViewModel::class.java]
                    )
                }
                .forClass(ProjectSettingsDialog::class) {
                    ProjectSettingsDialog(viewModelFactory)
                }
                .forClass(MainMenuFragment::class) {
                    MainMenuFragment(viewModelFactory, settingsProvider)
                }
                .build()

            super.onCreate(savedInstanceState)
            setContentView(R.layout.main_menu_activity)
        }
    }

    private fun initSplashScreen() {
        /*
        We don't need the `installSplashScreen` call on Android 12+ (the system handles the
        splash screen for us) and it causes problems if we later switch between dark/light themes
        with the ThemeUtils#setDarkModeForCurrentProject call.
         */
        if (Build.VERSION.SDK_INT < 31) {
            installSplashScreen()
        } else {
            setTheme(R.style.Theme_Collect)
        }
    }
}
