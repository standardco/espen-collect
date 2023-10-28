package org.espen.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.espen.collect.android.R;
import org.espen.collect.android.support.rules.CollectTestRule;
import org.espen.collect.android.support.rules.TestRuleChain;
import org.espen.collect.android.support.pages.ProjectSettingsPage;
import org.espen.collect.android.support.pages.MainMenuPage;

//Issue NODK-415
@RunWith(AndroidJUnit4.class)
public class TriggerWidgetTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void guidanceIcons_ShouldBeAlwaysShown() {
        rule.startAtMainMenu()
                .copyForm("Automated_guidance_hint_form.xml")
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(org.odk.collect.strings.R.string.guidance_yes)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .startBlankForm("Guidance Form Sample")
                .assertText("Guidance text")
                .swipeToEndScreen()
                .clickFinalize();

    }

    @Test
    public void guidanceIcons_ShouldBeCollapsed() {
        rule.startAtMainMenu()
                .copyForm("Automated_guidance_hint_form.xml")
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(org.odk.collect.strings.R.string.guidance_yes_collapsed)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .startBlankForm("Guidance Form Sample")
                .checkIsIdDisplayed(R.id.help_icon)
                .clickOnText("TriggerWidget")
                .assertText("Guidance text")
                .swipeToEndScreen()
                .clickFinalize();
    }
}
