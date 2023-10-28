package org.espen.collect.android

import androidx.test.core.app.ApplicationProvider
import org.espen.collect.android.application.EspenCollect
import org.espen.collect.android.injection.DaggerUtils
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.settings.Settings

// Use just for testing
object TestSettingsProvider {
    @JvmStatic
    fun getSettingsProvider(): SettingsProvider {
        return org.espen.collect.android.injection.DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<org.espen.collect.android.application.EspenCollect>()).settingsProvider()
    }

    @JvmStatic
    @JvmOverloads
    fun getUnprotectedSettings(uuid: String? = null): Settings {
        return getSettingsProvider().getUnprotectedSettings(uuid)
    }

    @JvmStatic
    fun getProtectedSettings(): Settings {
        return getSettingsProvider().getProtectedSettings()
    }
}
