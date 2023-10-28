package org.espen.collect.android.configure.qr

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import org.espen.collect.android.R
import org.espen.collect.android.utilities.FileProvider
import org.espen.collect.android.utilities.MenuDelegate
import org.espen.collect.androidshared.system.IntentLauncher
import org.espen.collect.androidshared.ui.ToastUtils.showShortToast
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.SettingsProvider
import timber.log.Timber

class QRCodeMenuDelegate internal constructor(
        private val activity: FragmentActivity,
        private val intentLauncher: IntentLauncher,
        qrCodeGenerator: QRCodeGenerator,
        appConfigurationGenerator: AppConfigurationGenerator,
        private val fileProvider: org.espen.collect.android.utilities.FileProvider,
        settingsProvider: SettingsProvider,
        scheduler: Scheduler
) : org.espen.collect.android.utilities.MenuDelegate {
    private var qrFilePath: String? = null

    init {
        val qrCodeViewModel = ViewModelProvider(
            activity,
            QRCodeViewModel.Factory(
                qrCodeGenerator,
                appConfigurationGenerator,
                settingsProvider,
                scheduler
            )
        )[QRCodeViewModel::class.java]

        qrCodeViewModel.filePath.observe(activity) { filePath: String? ->
            if (filePath != null) {
                qrFilePath = filePath
            }
        }
    }

    override fun onCreateOptionsMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.qr_code_scan_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_scan_sd_card -> {
                val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                photoPickerIntent.type = "image/*"
                intentLauncher.launchForResult(activity, photoPickerIntent, SELECT_PHOTO) {
                    showShortToast(
                        activity,
                        activity.getString(
                            org.odk.collect.strings.R.string.activity_not_found,
                            activity.getString(org.odk.collect.strings.R.string.choose_image)
                        )
                    )
                    Timber.w(
                        activity.getString(
                            org.odk.collect.strings.R.string.activity_not_found,
                            activity.getString(org.odk.collect.strings.R.string.choose_image)
                        )
                    )
                }
                return true
            }
            R.id.menu_item_share -> {
                if (qrFilePath != null) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, fileProvider.getURIForFile(qrFilePath))
                    }
                    activity.startActivity(intent)
                }
                return true
            }
        }
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu) = Unit

    companion object {
        const val SELECT_PHOTO = 111
    }
}
