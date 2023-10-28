package org.espen.collect.android.feature.formentry.audit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.csv.CSVRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.espen.collect.android.support.StorageUtils;
import org.espen.collect.android.support.pages.FormEntryPage;
import org.espen.collect.android.support.pages.FormHierarchyPage;
import org.espen.collect.android.support.pages.IdentifyUserPromptPage;
import org.espen.collect.android.support.pages.MainMenuPage;
import org.espen.collect.android.support.rules.CollectTestRule;
import org.espen.collect.android.support.rules.TestRuleChain;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class IdentifyUserTest {

    private static final String IDENTIFY_USER_AUDIT_FORM = "identify-user-audit.xml";
    private static final String IDENTIFY_USER_AUDIT_FALSE_FORM = "identify-user-audit-false.xml";

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void openingForm_andThenEnteringIdentity_andThenFillingForm_logsUser() throws IOException {
        rule.startAtMainMenu()
                .copyForm(IDENTIFY_USER_AUDIT_FORM)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Lucius")
                .clickKeyboardEnter(new FormEntryPage("Identify User"))
                .swipeToEndScreen()
                .clickFinalize();

        List<CSVRecord> auditLog = StorageUtils.getAuditLogForFirstInstance();
        CSVRecord formStartEvent = auditLog.get(0);
        assertThat(formStartEvent.get(0), equalTo("form start"));
        assertThat(formStartEvent.get(4), equalTo("Lucius"));
    }

    @Test
    public void openingSavedForm_andThenEnteringIdentity_andThenFillingForm_logsUser() throws IOException {
        rule.startAtMainMenu()
                .copyForm(IDENTIFY_USER_AUDIT_FORM)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Lucius")
                .clickKeyboardEnter(new FormEntryPage("Identify User"))
                .swipeToEndScreen()
                .clickSaveAsDraft()

                .clickEditSavedForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Jack")
                .clickKeyboardEnter(new FormHierarchyPage("Identify User"))
                .clickJumpEndButton()
                .clickFinalize();

            List<CSVRecord> auditLog = StorageUtils.getAuditLogForFirstInstance();
            CSVRecord formResumeEvent = auditLog.get(5);
            assertThat(formResumeEvent.get(0), equalTo("form resume"));
            assertThat(formResumeEvent.get(4), equalTo("Jack"));
    }

    @Test
    public void openingForm_andEnteringBlankIdentity_remainsOnIdentityPrompt() {
        rule.startAtMainMenu()
                .copyForm(IDENTIFY_USER_AUDIT_FORM)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("  ")
                .clickKeyboardEnterWithValidationError();
    }

    @Test
    public void openingForm_andPressingBack_returnsToMainMenu() {
        rule.startAtMainMenu()
                .copyForm(IDENTIFY_USER_AUDIT_FORM)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .closeSoftKeyboard()
                .pressBack(new MainMenuPage());
    }

    @Test
    public void openingForm_andRotating_remainsOnIdentityPrompt() {
        rule.startAtMainMenu()
                .copyForm(IDENTIFY_USER_AUDIT_FORM)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Blah")
                .rotateToLandscape(new IdentifyUserPromptPage("Identify User"))
                .assertText("Blah");
    }

    @Test
    public void openingForm_andPressingCloseCross_returnsToMainMenu() {
        rule.startAtMainMenu()
                .copyForm(IDENTIFY_USER_AUDIT_FORM)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .pressClose();
    }

    @Test
    public void openFormWithIdentifyUserFalse_proceedsToForm() {
        rule.startAtMainMenu()
                .copyForm(IDENTIFY_USER_AUDIT_FALSE_FORM)
                .clickFillBlankForm()
                .clickOnForm("Identify User False")
                .swipeToEndScreen()
                .clickFinalize();
    }
}
