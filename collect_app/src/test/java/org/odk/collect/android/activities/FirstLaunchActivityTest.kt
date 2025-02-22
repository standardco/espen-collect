package org.espen.collect.android.activities

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.espen.collect.android.R
import org.espen.collect.android.application.EspenCollect
import org.espen.collect.android.injection.config.AppDependencyModule
import org.espen.collect.android.projects.ManualProjectCreatorDialog
import org.espen.collect.android.projects.QrCodeProjectCreatorDialog
import org.espen.collect.android.support.CollectHelpers
import org.espen.collect.android.version.VersionInformation
import org.espen.collect.androidtest.ActivityScenarioLauncherRule
import org.espen.collect.androidtest.RecordedIntentsRule
import org.odk.collect.strings.localization.getLocalizedString

@RunWith(AndroidJUnit4::class)
class FirstLaunchActivityTest {

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @get:Rule
    val activityRule = RecordedIntentsRule()

    @Test
    fun `The QrCodeProjectCreatorDialog should be displayed after clicking on the 'Configure with QR code' button`() {
        val scenario = launcherRule.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withText(org.odk.collect.strings.R.string.configure_with_qr_code)).perform(click())
            assertThat(
                it.supportFragmentManager.findFragmentByTag(QrCodeProjectCreatorDialog::class.java.name),
                `is`(notNullValue())
            )
        }
    }

    @Test
    fun `The ManualProjectCreatorDialog should be displayed after clicking on the 'Configure manually' button`() {
        val scenario = launcherRule.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withText(org.odk.collect.strings.R.string.configure_manually)).perform(click())
            assertThat(
                it.supportFragmentManager.findFragmentByTag(ManualProjectCreatorDialog::class.java.name),
                `is`(notNullValue())
            )
        }
    }

    @Test
    fun `The ODK logo should be displayed`() {
        val scenario = launcherRule.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withId(R.id.logo)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `The app name with its version should be displayed`() {
        val versionInformation = mock(org.espen.collect.android.version.VersionInformation::class.java)
        whenever(versionInformation.versionToDisplay).thenReturn("vfake")
        CollectHelpers.overrideAppDependencyModule(object : org.espen.collect.android.injection.config.AppDependencyModule() {
            override fun providesVersionInformation(): org.espen.collect.android.version.VersionInformation {
                return versionInformation
            }
        })

        val scenario = launcherRule.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            verify(versionInformation).versionToDisplay
            onView(
                withText(
                    ApplicationProvider.getApplicationContext<org.espen.collect.android.application.EspenCollect>().getLocalizedString(
                        org.odk.collect.strings.R.string.collect_app_name
                    ) + " vfake"
                )
            ).check(matches(isDisplayed()))
        }
    }
}
