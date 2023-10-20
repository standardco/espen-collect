package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.ErrorPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.SendFinalizedFormPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.NotificationDrawerRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class AutoSendTest {
    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()
    private val notificationDrawerRule = NotificationDrawerRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(notificationDrawerRule)
        .around(rule)

    @Test
    fun whenAutoSendEnabled_fillingAndFinalizingForm_sendsFormAndNotifiesUser() {
        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableAutoSend()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage
            .clickViewSentForm(1)
            .assertText("One Question")

        notificationDrawerRule
            .open()
            .assertNotification("ODK EspenCollect", "Forms upload succeeded", "All uploads succeeded!")
            .clickNotification(
                "ODK EspenCollect",
                "Forms upload succeeded",
                MainMenuPage()
            )
    }

    @Test
    fun whenAutoSendEnabled_fillingAndFinalizingForm_notifiesUserWhenSendingFails() {
        testDependencies.server.alwaysReturnError()

        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableAutoSend()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage
            .clickViewSentForm(1)
            .assertText("One Question")

        notificationDrawerRule
            .open()
            .assertNotification("ODK EspenCollect", "Forms upload failed", "1 of 1 uploads failed!")
            .clickAction(
                "ODK EspenCollect",
                "Show details",
                ErrorPage()
            )

        notificationDrawerRule
            .open()
            .clickNotification(
                "ODK EspenCollect",
                "Forms upload failed",
                SendFinalizedFormPage()
            )
    }

    @Test
    fun whenFormHasAutoSend_fillingAndFinalizingForm_sendsFormAndNotifiesUser() {
        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question-autosend.xml")
            .startBlankForm("One Question Autosend")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage
            .clickViewSentForm(1)
            .assertText("One Question Autosend")

        notificationDrawerRule
            .open()
            .assertNotification("ODK EspenCollect", "Forms upload succeeded", "All uploads succeeded!")
            .clickNotification(
                "ODK EspenCollect",
                "Forms upload succeeded",
                MainMenuPage()
            )
    }

    @Test
    fun whenFormHasAutoSend_fillingAndFinalizingForm_notifiesUserWhenSendingFails() {
        testDependencies.server.alwaysReturnError()

        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question-autosend.xml")
            .startBlankForm("One Question Autosend")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage
            .clickViewSentForm(1)
            .assertText("One Question Autosend")

        notificationDrawerRule
            .open()
            .assertNotification("ODK EspenCollect", "Forms upload failed", "1 of 1 uploads failed!")
            .clickNotification(
                "ODK EspenCollect",
                "Forms upload failed",
                SendFinalizedFormPage()
            )
    }
}
