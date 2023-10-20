package org.odk.collect.android

import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.application.EspenCollect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.settings.Settings

// Use just for testing
object TestSettingsProvider {
    @JvmStatic
    fun getSettingsProvider(): SettingsProvider {
        return DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<EspenCollect>()).settingsProvider()
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
