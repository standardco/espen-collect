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
import org.espen.collect.android.support.pages.SaveOrIgnoreDrawingDialog;

// Issue number NODK-211
@RunWith(AndroidJUnit4.class)
public class SignatureWidgetTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void saveIgnoreDialog_ShouldUseBothOptions() {

        //TestCase1
        rule.startAtMainMenu()
                .copyForm("all-widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnQuestion("Signature widget")
                .clickWidgetButton()
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDrawingDialog<>("Gather Signature", new FormEntryPage("All widgets")))
                .checkIsTranslationDisplayed("Exit Gather Signature", "Salir Adjuntar firma")
                .assertText(org.odk.collect.strings.R.string.keep_changes)
                .clickDiscardChanges()
                .waitForRotationToEnd()
                .clickWidgetButton()
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDrawingDialog<>("Gather Signature", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickFinalize();
    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase2
        rule.startAtMainMenu()
                .copyForm("all-widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnQuestion("Signature widget")
                .clickWidgetButton()
                .waitForRotationToEnd()
                .clickOnId(R.id.fab_actions)
                .checkIsIdDisplayed(R.id.fab_save_and_close)
                .clickOnId(R.id.fab_set_color)
                .clickOnString(org.odk.collect.strings.R.string.ok)
                .clickOnId(R.id.fab_actions)
                .checkIsIdDisplayed(R.id.fab_set_color)
                .pressBack(new SaveOrIgnoreDrawingDialog<>("Gather Signature", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickFinalize();
    }
}
