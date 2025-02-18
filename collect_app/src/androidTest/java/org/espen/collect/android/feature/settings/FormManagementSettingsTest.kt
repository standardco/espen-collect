package org.odk.collect.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidtest.RecordedIntentsRule

@RunWith(AndroidJUnit4::class)
class FormManagementSettingsTest {
    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun whenMatchExactlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        var deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks, `is`(empty()))

        val page = MainMenuPage().assertOnPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickUpdateForms()
            .clickOption(org.odk.collect.strings.R.string.match_exactly)

        deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks.size, `is`(1))

        val matchExactlyTag = deferredTasks[0].tag

        page.clickAutomaticUpdateFrequency().clickOption(org.odk.collect.strings.R.string.every_one_hour)
        deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks.size, `is`(1))
        assertThat(deferredTasks[0].tag, `is`(matchExactlyTag))
        assertThat(deferredTasks[0].repeatPeriod, `is`(1000L * 60 * 60))
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        var deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks, `is`(empty()))

        val page = MainMenuPage().assertOnPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickUpdateForms()
            .clickOption(org.odk.collect.strings.R.string.previously_downloaded_only)

        deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks.size, `is`(1))

        val previouslyDownloadedTag = deferredTasks[0].tag
        page.clickAutomaticUpdateFrequency().clickOption(org.odk.collect.strings.R.string.every_one_hour)

        deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks.size, `is`(1))
        assertThat(deferredTasks[0].tag, `is`(previouslyDownloadedTag))
        assertThat(deferredTasks[0].repeatPeriod, `is`(1000L * 60 * 60))
    }
}
