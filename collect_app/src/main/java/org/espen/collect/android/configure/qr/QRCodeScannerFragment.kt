package org.espen.collect.android.configure.qr

import android.content.Context
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeResult
import org.odk.collect.analytics.Analytics
import org.espen.collect.android.activities.ActivityUtils
import org.espen.collect.android.analytics.AnalyticsEvents
import org.espen.collect.android.fragments.BarCodeScannerFragment
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.mainmenu.MainMenuActivity
import org.espen.collect.android.projects.ProjectsDataService
import org.espen.collect.android.storage.StoragePathProvider
import org.espen.collect.androidshared.ui.ToastUtils.showLongToast
import org.espen.collect.androidshared.utils.CompressionUtils
import org.odk.collect.settings.ODKAppSettingsImporter
import org.odk.collect.settings.importing.SettingsImportingResult
import java.io.File
import java.io.IOException
import java.util.zip.DataFormatException
import javax.inject.Inject

class QRCodeScannerFragment : org.espen.collect.android.fragments.BarCodeScannerFragment() {

    @Inject
    lateinit var settingsImporter: ODKAppSettingsImporter

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var storagePathProvider: StoragePathProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)
        org.espen.collect.android.injection.DaggerUtils.getComponent(context).inject(this)
    }

    @Throws(IOException::class, DataFormatException::class)
    override fun handleScanningResult(result: BarcodeResult) {
        val oldProjectName = projectsDataService.getCurrentProject().name

        val settingsImportingResult = settingsImporter.fromJSON(
            CompressionUtils.decompress(result.text),
            projectsDataService.getCurrentProject()
        )

        when (settingsImportingResult) {
            SettingsImportingResult.SUCCESS -> {
                Analytics.log(AnalyticsEvents.RECONFIGURE_PROJECT)

                val newProjectName = projectsDataService.getCurrentProject().name
                if (newProjectName != oldProjectName) {
                    File(storagePathProvider.getProjectRootDirPath() + File.separator + oldProjectName).delete()
                    File(storagePathProvider.getProjectRootDirPath() + File.separator + newProjectName).createNewFile()
                }

                showLongToast(
                    requireContext(),
                    getString(org.odk.collect.strings.R.string.successfully_imported_settings)
                )
                org.espen.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                    requireActivity(),
                    MainMenuActivity::class.java
                )
            }

            SettingsImportingResult.INVALID_SETTINGS -> showLongToast(
                requireContext(),
                getString(
                    org.odk.collect.strings.R.string.invalid_qrcode
                )
            )

            SettingsImportingResult.GD_PROJECT -> showLongToast(
                requireContext(),
                getString(org.odk.collect.strings.R.string.settings_with_gd_protocol)
            )
        }
    }

    override fun getSupportedCodeFormats(): Collection<String> {
        return listOf(IntentIntegrator.QR_CODE)
    }
}
