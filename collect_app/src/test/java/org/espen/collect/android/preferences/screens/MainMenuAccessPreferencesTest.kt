package org.odk.collect.android.preferences.screens

import android.content.Context
import androidx.preference.CheckBoxPreference
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.settings.enums.FormUpdateMode
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.shared.settings.Settings

@RunWith(AndroidJUnit4::class)
class MainMenuAccessPreferencesTest {
    private lateinit var context: Context
    private lateinit var generalSettings: Settings
    private lateinit var adminSettings: Settings

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()
        context = ApplicationProvider.getApplicationContext()
        generalSettings = TestSettingsProvider.getUnprotectedSettings()
        adminSettings = TestSettingsProvider.getProtectedSettings()
    }

    @Test
    fun `Edit Saved Form option should be checked if it's enabled in settings`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_EDIT_SAVED)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `Edit Saved Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(ProtectedProjectKeys.KEY_EDIT_SAVED, false)

        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_EDIT_SAVED)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the Edit Saved Form option should change its state`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_EDIT_SAVED)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the Edit Saved Form option`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_EDIT_SAVED)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `Edit Saved Form option should be disabled if editing forms is disabled`() {
        adminSettings.save(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM, false)

        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_EDIT_SAVED)!!.isEnabled, `is`(false))
        }
    }

    @Test
    fun `Send Finalized Form option should be checked if it's enabled in settings`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SEND_FINALIZED)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `Send Finalized Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(ProtectedProjectKeys.KEY_SEND_FINALIZED, false)

        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SEND_FINALIZED)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the Send Finalized Form option should change its state`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SEND_FINALIZED)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the Send Finalized Form option`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SEND_FINALIZED)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `View Sent Form option should be checked if it's enabled in settings`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_VIEW_SENT)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `View Sent Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(ProtectedProjectKeys.KEY_VIEW_SENT, false)

        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_VIEW_SENT)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the View Sent Form option should change its state`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_VIEW_SENT)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the View Sent Form option`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_VIEW_SENT)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `Get Blank Form option should be checked if it's enabled in settings`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_GET_BLANK)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `Get Blank Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(ProtectedProjectKeys.KEY_GET_BLANK, false)

        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_GET_BLANK)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the Get Blank Form option should change its state`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_GET_BLANK)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the Get Blank Form option`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_GET_BLANK)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `Delete Saved Form option should be checked if it's enabled in settings`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_DELETE_SAVED)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `Delete Saved Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(ProtectedProjectKeys.KEY_DELETE_SAVED, false)

        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_DELETE_SAVED)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the Delete Saved Form option should change its state`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_DELETE_SAVED)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the Delete Saved Form option`() {
        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_DELETE_SAVED)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `When match exactly enabled shows the Get Blank Form option as unchecked and disabled`() {
        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))

        val scenario = launcherRule.launch(MainMenuAccessPreferencesFragment::class.java)

        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_GET_BLANK)
            assertThat(option!!.isEnabled, `is`(false))
            assertThat(option.isChecked, `is`(false))
            assertThat(adminSettings.getBoolean(ProtectedProjectKeys.KEY_GET_BLANK), `is`(true))
        }
    }
}
