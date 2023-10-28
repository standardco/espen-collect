package org.espen.collect.android.configure.qr

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.espen.collect.android.TestSettingsProvider.getProtectedSettings
import org.espen.collect.android.TestSettingsProvider.getUnprotectedSettings
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class QRCodeViewModelTest {
    private val qrCodeGenerator = mock<QRCodeGenerator>()
    private val appConfigurationGenerator = mock<AppConfigurationGenerator>()
    private val fakeScheduler = FakeScheduler()
    private val generalSettings = getUnprotectedSettings()
    private val adminSettings = getProtectedSettings()

    private lateinit var viewModel: QRCodeViewModel

    @Before
    fun setup() {
        viewModel = QRCodeViewModel(
            qrCodeGenerator,
            appConfigurationGenerator,
            generalSettings,
            adminSettings,
            fakeScheduler
        )
    }

    @Test
    fun setIncludedKeys_generatesQRCodeWithKeys() {
        viewModel.setIncludedKeys(listOf("foo", "bar"))
        fakeScheduler.runBackground()

        verify(qrCodeGenerator).generateQRCode(listOf("foo", "bar"), appConfigurationGenerator)
    }

    @Test
    fun warning_whenNeitherServerOrAdminPasswordSet_isNull() {
        assertThat(viewModel.warning.value, nullValue())
    }

    @Test
    fun warning_whenServerAndAdminPasswordSet_isForBoth() {
        generalSettings.save(ProjectKeys.KEY_PASSWORD, "blah")
        adminSettings.save(ProtectedProjectKeys.KEY_ADMIN_PW, "blah")

        fakeScheduler.runBackground()
        assertThat(viewModel.warning.value, equalTo(org.odk.collect.strings.R.string.qrcode_with_both_passwords))
    }
}
