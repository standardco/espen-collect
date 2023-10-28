package org.espen.collect.android.configure.qr

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.espen.collect.android.R
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.projects.ProjectsDataService
import org.espen.collect.android.utilities.FileProvider
import org.espen.collect.androidshared.system.IntentLauncher
import org.espen.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import org.espen.collect.androidshared.utils.AppBarUtils.setupAppBarLayout
import org.odk.collect.async.Scheduler
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.qrcode.QRCodeDecoder
import org.odk.collect.settings.ODKAppSettingsImporter
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class QRCodeTabsActivity : LocalizedActivity() {
    @Inject
    lateinit var qrCodeGenerator: QRCodeGenerator

    @Inject
    lateinit var intentLauncher: IntentLauncher

    @Inject
    lateinit var fileProvider: org.espen.collect.android.utilities.FileProvider

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var qrCodeDecoder: QRCodeDecoder

    @Inject
    lateinit var settingsImporter: ODKAppSettingsImporter

    @Inject
    lateinit var appConfigurationGenerator: AppConfigurationGenerator

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    private lateinit var menuDelegate: QRCodeMenuDelegate
    private lateinit var activityResultDelegate: QRCodeActivityResultDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.espen.collect.android.injection.DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.tabs_layout)
        setupAppBarLayout(this, getString(org.odk.collect.strings.R.string.reconfigure_with_qr_code_settings_title))

        activityResultDelegate = QRCodeActivityResultDelegate(
            this,
            settingsImporter,
            qrCodeDecoder,
            projectsDataService.getCurrentProject()
        )

        menuDelegate = QRCodeMenuDelegate(
            this,
            intentLauncher,
            qrCodeGenerator,
            appConfigurationGenerator,
            fileProvider,
            settingsProvider,
            scheduler
        )

        permissionsProvider.requestCameraPermission(
            this,
            object : PermissionListener {
                override fun granted() {
                    setupViewPager()
                }

                override fun additionalExplanationClosed() {
                    finish()
                }
            }
        )
    }

    private fun setupViewPager() {
        val fragmentTitleList = arrayOf(
            getString(org.odk.collect.strings.R.string.scan_qr_code_fragment_title),
            getString(org.odk.collect.strings.R.string.view_qr_code_fragment_title)
        )

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val adapter = QRCodeTabsAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = fragmentTitleList[position]
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuDelegate.onCreateOptionsMenu(menuInflater, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!allowClick(javaClass.name)) {
            return true
        }

        return if (menuDelegate.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResultDelegate.onActivityResult(requestCode, resultCode, data)
    }
}
