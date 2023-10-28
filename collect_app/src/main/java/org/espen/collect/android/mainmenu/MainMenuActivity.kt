package org.espen.collect.android.mainmenu

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.espen.collect.android.R
import org.espen.collect.android.activities.ActivityUtils
import org.espen.collect.android.activities.CrashHandlerActivity
import org.espen.collect.android.activities.DeleteSavedFormActivity
import org.espen.collect.android.activities.FirstLaunchActivity
import org.espen.collect.android.activities.FormDownloadListActivity
import org.espen.collect.android.activities.InstanceChooserList
import org.espen.collect.android.activities.WebViewActivity
import org.espen.collect.android.application.MapboxClassInstanceCreator.createMapBoxInitializationFragment
import org.espen.collect.android.application.MapboxClassInstanceCreator.isMapboxAvailable
import org.espen.collect.android.databinding.MainMenuBinding
import org.espen.collect.android.formlists.blankformlist.BlankFormListActivity
import org.espen.collect.android.formmanagement.FormFillingIntentFactory
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.instancemanagement.send.InstanceUploaderListActivity
import org.espen.collect.android.projects.ProjectIconView
import org.espen.collect.android.projects.ProjectSettingsDialog
import org.espen.collect.android.utilities.ApplicationConstants
import org.espen.collect.android.utilities.ThemeUtils
import org.espen.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.espen.collect.androidshared.ui.FragmentFactoryBuilder
import org.espen.collect.androidshared.ui.SnackbarUtils
import org.espen.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import org.odk.collect.crashhandler.CrashHandler
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.projects.Project.Saved
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class MainMenuActivity : LocalizedActivity() {

    @Inject
    lateinit var viewModelFactory: MainMenuViewModelFactory

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    private lateinit var binding: MainMenuBinding
    private lateinit var mainMenuViewModel: MainMenuViewModel
    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    private val formLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            displayFormSavedSnackbar(it.data?.data)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        initSplashScreen()

        CrashHandler.getInstance(this)?.also {
            if (it.hasCrashed(this)) {
                super.onCreate(null)
                org.espen.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers(this, CrashHandlerActivity::class.java)
                return
            }
        }

        org.espen.collect.android.injection.DaggerUtils.getComponent(this).inject(this)

        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        mainMenuViewModel = viewModelProvider[MainMenuViewModel::class.java]
        currentProjectViewModel = viewModelProvider[CurrentProjectViewModel::class.java]
        val permissionsViewModel = viewModelProvider[RequestPermissionsViewModel::class.java]

        this.supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(PermissionsDialogFragment::class) {
                PermissionsDialogFragment(
                    permissionsProvider,
                    permissionsViewModel
                )
            }
            .forClass(ProjectSettingsDialog::class) {
                ProjectSettingsDialog(viewModelFactory)
            }
            .build()

        super.onCreate(savedInstanceState)
        binding = MainMenuBinding.inflate(layoutInflater)

        org.espen.collect.android.utilities.ThemeUtils(this).setDarkModeForCurrentProject()

        if (!currentProjectViewModel.hasCurrentProject()) {
            org.espen.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers(this, FirstLaunchActivity::class.java)
            return
        }

        setContentView(binding.root)

        currentProjectViewModel.currentProject.observe(this) { (_, name): Saved ->
            invalidateOptionsMenu()
            title = name
        }

        initToolbar()
        initMapbox()
        initButtons()
        initAppName()

        if (permissionsViewModel.shouldAskForPermissions()) {
            showIfNotShowing(PermissionsDialogFragment::class.java, this.supportFragmentManager)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFinishing) {
            return // Guard against onResume calls after we've finished in onCreate
        }

        currentProjectViewModel.refresh()
        mainMenuViewModel.refreshInstances()
        setButtonsVisibility()
        manageGoogleDriveDeprecationBanner()
    }

    private fun setButtonsVisibility() {
        binding.reviewData.visibility =
            if (mainMenuViewModel.shouldEditSavedFormButtonBeVisible()) View.VISIBLE else View.GONE
        binding.sendData.visibility =
            if (mainMenuViewModel.shouldSendFinalizedFormButtonBeVisible()) View.VISIBLE else View.GONE
        binding.viewSentForms.visibility =
            if (mainMenuViewModel.shouldViewSentFormButtonBeVisible()) View.VISIBLE else View.GONE
        binding.getForms.visibility =
            if (mainMenuViewModel.shouldGetBlankFormButtonBeVisible()) View.VISIBLE else View.GONE
        binding.manageForms.visibility =
            if (mainMenuViewModel.shouldDeleteSavedFormButtonBeVisible()) View.VISIBLE else View.GONE
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val projectsMenuItem = menu.findItem(R.id.projects)
        (projectsMenuItem.actionView as ProjectIconView).apply {
            project = currentProjectViewModel.currentProject.value
            setOnClickListener { onOptionsItemSelected(projectsMenuItem) }
            contentDescription = getString(org.odk.collect.strings.R.string.projects)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!allowClick(org.espen.collect.android.utilities.ApplicationConstants.ScreenName.MAIN_MENU.name)) {
            return true
        }
        if (item.itemId == R.id.projects) {
            showIfNotShowing(ProjectSettingsDialog::class.java, supportFragmentManager)
            return true
        }
        return super.onOptionsItemSelected(item)
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

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun initMapbox() {
        if (isMapboxAvailable()) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.map_box_initialization_fragment, createMapBoxInitializationFragment())
                .commit()
        }
    }

    private fun initButtons() {
        binding.enterData.setOnClickListener {
            formLauncher.launch(
                Intent(this, BlankFormListActivity::class.java)
            )
        }

        binding.reviewData.setOnClickListener {
            formLauncher.launch(
                Intent(this, org.espen.collect.android.activities.InstanceChooserList::class.java).apply {
                    putExtra(
                        org.espen.collect.android.utilities.ApplicationConstants.BundleKeys.FORM_MODE,
                        org.espen.collect.android.utilities.ApplicationConstants.FormModes.EDIT_SAVED
                    )
                }
            )
        }

        binding.sendData.setOnClickListener {
            startActivity(Intent(this, org.espen.collect.android.instancemanagement.send.InstanceUploaderListActivity::class.java))
        }

        binding.viewSentForms.setOnClickListener {
            startActivity(
                Intent(this, org.espen.collect.android.activities.InstanceChooserList::class.java).apply {
                    putExtra(
                        org.espen.collect.android.utilities.ApplicationConstants.BundleKeys.FORM_MODE,
                        org.espen.collect.android.utilities.ApplicationConstants.FormModes.VIEW_SENT
                    )
                }
            )
        }

        binding.getForms.setOnClickListener {
            val protocol =
                settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_PROTOCOL)
            if (!protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS, ignoreCase = true)) {
                val intent = Intent(
                    applicationContext,
                    org.espen.collect.android.activities.FormDownloadListActivity::class.java
                )

                startActivity(intent)
            } else {
                MaterialAlertDialogBuilder(this)
                    .setMessage(org.odk.collect.strings.R.string.cannot_start_new_forms_in_google_drive_projects)
                    .setPositiveButton(org.odk.collect.strings.R.string.ok, null)
                    .create()
                    .show()
            }
        }

        binding.manageForms.setOnClickListener {
            startActivity(Intent(this, DeleteSavedFormActivity::class.java))
        }

        mainMenuViewModel.sendableInstancesCount.observe(this) { finalized: Int ->
            binding.sendData.setNumberOfForms(finalized)
        }
        mainMenuViewModel.editableInstancesCount.observe(this) { unsent: Int ->
            binding.reviewData.setNumberOfForms(unsent)
        }
        mainMenuViewModel.sentInstancesCount.observe(this) { sent: Int ->
            binding.viewSentForms.setNumberOfForms(sent)
        }
    }

    private fun initAppName() {
        binding.appName.text = String.format(
            "%s %s",
            getString(org.odk.collect.strings.R.string.collect_app_name),
            mainMenuViewModel.version
        )

        val versionSHA = mainMenuViewModel.versionCommitDescription
        if (versionSHA != null) {
            binding.versionSha.text = versionSHA
        } else {
            binding.versionSha.visibility = View.GONE
        }
    }

    private fun manageGoogleDriveDeprecationBanner() {
        val unprotectedSettings = settingsProvider.getUnprotectedSettings()
        val protocol = unprotectedSettings.getString(ProjectKeys.KEY_PROTOCOL)
        if (ProjectKeys.PROTOCOL_GOOGLE_SHEETS == protocol) {
            binding.googleDriveDeprecationBanner.root.visibility = View.VISIBLE
            binding.googleDriveDeprecationBanner.learnMoreButton.setOnClickListener {
                val intent = Intent(this, org.espen.collect.android.activities.WebViewActivity::class.java)
                intent.putExtra("url", "https://forum.getodk.org/t/40097")
                startActivity(intent)
            }
        } else {
            binding.googleDriveDeprecationBanner.root.visibility = View.GONE
        }
    }

    private fun displayFormSavedSnackbar(uri: Uri?) {
        if (uri == null) {
            return
        }

        val formSavedSnackbarDetails = mainMenuViewModel.getFormSavedSnackbarDetails(uri)

        formSavedSnackbarDetails?.let {
            SnackbarUtils.showLongSnackbar(
                binding.root,
                getString(it.first),
                action = it.second?.let { action ->
                    SnackbarUtils.Action(getString(action)) {
                        formLauncher.launch(FormFillingIntentFactory.editInstanceIntent(this, uri))
                    }
                },
                displayDismissButton = true
            )
        }
    }
}
