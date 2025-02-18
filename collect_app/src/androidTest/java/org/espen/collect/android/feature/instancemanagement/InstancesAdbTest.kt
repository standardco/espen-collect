package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import java.io.File

@RunWith(AndroidJUnit4::class)
class InstancesAdbTest {

    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies).around(rule)

    @Test
    fun canAddInstanceOnDisk() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyInstance("One Question_2021-06-22_15-55-50.xml")
            .clickFillBlankForm() // Add form via disk sync
            .pressBack(MainMenuPage()) // Return to main menu to trigger instance disk sync
            .clickSendFinalizedForm(1)
            .assertText("One Question")
    }

    @Test
    fun deletingInstanceOnDisk_andThenOpeningInstance_showsWarning_andRemovesInstance() {
        val mainMenuPage = rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()

        val instancesDir =
            testDependencies.storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES)
        val instanceDeleted = File(instancesDir).listFiles()[0].deleteRecursively()
        Assert.assertTrue(instanceDeleted)

        mainMenuPage
            .clickDrafts(1)
            .clickOnFormWithDialog("One Question")
            .assertText(org.odk.collect.strings.R.string.instance_deleted_message)
            .clickOK(MainMenuPage())
            .assertNumberOfEditableForms(0)
    }
}
