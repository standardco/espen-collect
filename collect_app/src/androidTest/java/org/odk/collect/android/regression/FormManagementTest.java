package org.espen.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.espen.collect.android.R;
import org.espen.collect.android.support.rules.CollectTestRule;
import org.espen.collect.android.support.rules.TestRuleChain;
import org.espen.collect.android.support.pages.FormEntryPage;
import org.espen.collect.android.support.pages.MainMenuPage;
import org.espen.collect.android.support.pages.ProjectSettingsPage;

//Issue NODK-237
@RunWith(AndroidJUnit4.class)
public class FormManagementTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @SuppressWarnings("PMD.AvoidCallingFinalize")
    @Test
    public void validationUponSwipe_ShouldDisplay() {
        //TestCase7,8
        rule.startAtMainMenu()
                .copyForm("OnePageFormValid2.xml")
                .startBlankForm("OnePageFormValid")
                .inputText("Bla")
                .swipeToNextQuestionWithConstraintViolation("Response length must be between 5 and 15")
                .clickOptionsIcon()
                .clickGeneralSettings()
                .openFormManagement()
                .openConstraintProcessing()
                .clickOnString(org.odk.collect.strings.R.string.constraint_behavior_on_finalize)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new FormEntryPage("OnePageFormValid"))
                .swipeToEndScreen()
                .clickSaveAndExitWithError("Response length must be between 5 and 15");
    }

    @Test
    public void guidanceForQuestion_ShouldDisplayAlways() {
        //TestCase10
        rule.startAtMainMenu()
                .copyForm("hints_textq.xml")
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(org.odk.collect.strings.R.string.guidance_yes)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .startBlankForm("hints textq")
                .assertText("1 very very very very very very very very very very long text")
                .swipeToEndScreen()
                .clickFinalize();
    }

    @Test
    public void guidanceForQuestion_ShouldBeCollapsed() {
        //TestCase11
        rule.startAtMainMenu()
                .copyForm("hints_textq.xml")
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(org.odk.collect.strings.R.string.guidance_yes_collapsed)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .startBlankForm("hints textq")
                .checkIsIdDisplayed(R.id.help_icon)
                .clickOnText("Hint 1")
                .assertText("1 very very very very very very very very very very long text")
                .swipeToEndScreen()
                .clickFinalize();
    }

}
