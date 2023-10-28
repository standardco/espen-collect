package org.espen.collect.android.feature.formmanagement

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.espen.collect.android.R
import org.espen.collect.android.support.TestDependencies
import org.espen.collect.android.support.pages.ErrorPage
import org.espen.collect.android.support.pages.FillBlankFormPage
import org.espen.collect.android.support.pages.GetBlankFormPage
import org.espen.collect.android.support.pages.MainMenuPage
import org.espen.collect.android.support.rules.CollectTestRule
import org.espen.collect.android.support.rules.NotificationDrawerRule
import org.espen.collect.android.support.rules.TestRuleChain
import org.odk.collect.projects.Project

class PreviouslyDownloadedOnlyTest {
    private val testDependencies = TestDependencies()
    private val notificationDrawerRule = NotificationDrawerRule()
    private val rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(notificationDrawerRule)
        .around(rule)

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_notifiesOnFormUpdates_automaticallyAndRepeatedly() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyForm("two-question.xml")
            .setServer(testDependencies.server.url)
            .enablePreviouslyDownloadedOnlyUpdates()

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )
        testDependencies.scheduler.runDeferredTasks()

        notificationDrawerRule.open()
            .assertNotification("ODK EspenCollect", "Form updates available", Project.DEMO_PROJECT_NAME)
            .clearAll()

        testDependencies.server.addForm(
            "Two Question Updated",
            "two_question",
            "1",
            "two-question-updated.xml"
        )
        testDependencies.scheduler.runDeferredTasks()

        notificationDrawerRule.open()
            .assertNotification("ODK EspenCollect", "Form updates available", Project.DEMO_PROJECT_NAME)
            .clickNotification(
                "ODK EspenCollect",
                "Form updates available",
                GetBlankFormPage()
            )
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabledWithAutomaticDownload_checkingAutoDownload_downloadsUpdatedForms_andDisplaysNotification() {
        val page = MainMenuPage().assertOnPage()
            .setServer(testDependencies.server.url)
            .enablePreviouslyDownloadedOnlyUpdatesWithAutomaticDownload()
            .copyForm("one-question.xml")

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        testDependencies.scheduler.runDeferredTasks()

        page.clickFillBlankForm()
            .assertText("One Question Updated")

        notificationDrawerRule.open()
            .assertNotification(
                "ODK EspenCollect",
                "Forms download succeeded",
                "All downloads succeeded!"
            )
            .clickNotification(
                "ODK EspenCollect",
                "Forms download succeeded",
                FillBlankFormPage()
            )
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabledWithAutomaticDownload_checkingAutoDownload_downloadsUpdatedForms_andDisplaysNotificationWhenFails() {
        testDependencies.server.errorOnFetchingForms()

        val page = MainMenuPage().assertOnPage()
            .setServer(testDependencies.server.url)
            .enablePreviouslyDownloadedOnlyUpdatesWithAutomaticDownload()
            .copyForm("one-question.xml")

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        testDependencies.scheduler.runDeferredTasks()

        page.clickFillBlankForm()
            .assertFormDoesNotExist("One Question Updated")

        notificationDrawerRule.open()
            .assertNotification(
                "ODK EspenCollect",
                "Forms download failed",
                "1 of 1 downloads failed!"
            )
            .clickNotification(
                "ODK EspenCollect",
                "Forms download failed",
                ErrorPage()
            )
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_getBlankFormsIsAvailable() {
        rule.startAtMainMenu()
            .enablePreviouslyDownloadedOnlyUpdates()
            .assertText(org.odk.collect.strings.R.string.get_forms)
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_fillBlankFormRefreshButtonIsGone() {
        rule.startAtMainMenu()
            .enablePreviouslyDownloadedOnlyUpdates()
            .clickFillBlankForm()
        onView(withId(R.id.menu_refresh)).check(doesNotExist())
    }

    @Test
    fun whenPreviouslyDownloadedOnlyDisabled_stopsCheckingForUpdates() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enablePreviouslyDownloadedOnlyUpdates()
            .enableManualUpdates()

        assertThat(testDependencies.scheduler.deferredTasks, equalTo(emptyList()))
    }
}
