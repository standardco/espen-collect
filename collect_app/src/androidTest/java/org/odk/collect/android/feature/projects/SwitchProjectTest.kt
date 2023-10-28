package org.espen.collect.android.feature.projects

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.espen.collect.android.support.TestDependencies
import org.espen.collect.android.support.pages.EntitiesPage
import org.espen.collect.android.support.pages.ExperimentalPage
import org.espen.collect.android.support.pages.FormEntryPage
import org.espen.collect.android.support.pages.MainMenuPage
import org.espen.collect.android.support.pages.ProjectSettingsPage
import org.espen.collect.android.support.rules.CollectTestRule
import org.espen.collect.android.support.rules.TestRuleChain

class SwitchProjectTest {

    val rule = CollectTestRule()
    val testDependencies = TestDependencies()

    @get:Rule
    var chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(GrantPermissionRule.grant(Manifest.permission.CAMERA))
        .around(rule)

    @Test
    fun canSwitchActiveProjectToAnotherInList() {
        // Add project Turtle nesting
        rule.startAtMainMenu()
            .assertProjectIcon("D")
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            // Switch to Turtle nesting
            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "John / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")
            .selectProject("Demo project")
            .checkIsToastWithMessageDisplayed(org.odk.collect.strings.R.string.switched_project, "Demo project")
            .assertProjectIcon("D")
    }

    @Test
    fun switchingProject_switchesSettingsFormsInstancesAndEntities() {
        testDependencies.server.addForm("One Question Entity", "one-question-entity", "1", "one-question-entity.xml")

        rule.startAtMainMenu()
            // Copy and fill form
            .copyForm("two-question.xml")
            .startBlankForm("Two Question")
            .swipeToNextQuestion("What is your age?")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .clickEditSavedForm(1)
            .assertText("Two Question")
            .pressBack(MainMenuPage())

            // Create and switch to new project
            .assertProjectIcon("D")
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            // Set server and download form
            .setServer(testDependencies.server.url)
            .clickGetBlankForm()
            .clickGetSelected()
            .clickOKOnDialog(MainMenuPage())

            // Fill form
            .startBlankForm("One Question Entity")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Alice"))
            .clickSendFinalizedForm(1)
            .assertText("One Question Entity")
            .pressBack(MainMenuPage())

            .openEntityBrowser()
            .clickOnDataset("people")
            .assertEntity("full_name: Alice")
            .pressBack(EntitiesPage())
            .pressBack(ExperimentalPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())

            // Switch back to first project
            .openProjectSettingsDialog()
            .selectProject("Demo project")

            // Check server
            .openProjectSettingsDialog()
            .clickSettings()
            .clickServerSettings()
            .clickOnURL()
            .assertText("https://demo.getodk.org")
            .clickOKOnDialog()
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())

            // Check forms
            .clickFillBlankForm()
            .assertFormExists("Two Question")
            .pressBack(MainMenuPage())

            // Check instances
            .clickEditSavedForm(1)
            .assertText("Two Question")
            .pressBack(MainMenuPage())

            // Check entities
            .openEntityBrowser()
            .assertTextDoesNotExist("people")
    }
}
