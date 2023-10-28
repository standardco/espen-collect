package org.espen.collect.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.espen.collect.android.support.rules.CollectTestRule
import org.espen.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class MovingBackwardsTest {
    private val rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun whenMovingBackwardDisabledWithPreventingUsersFormBypassingIt_relatedOptionsShouldBeUpdated() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(org.odk.collect.strings.R.string.finalize)

            // before disabling moving backward
            .assertGoToPromptEnabled()
            .assertGoToPromptChecked()

            .assertSaveAsDraftInFormEntryEnabled()
            .assertSaveAsDraftInFormEntryChecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndChecked()

            .assertFinalizeEnabled()
            .assertFinalizeUnchecked()

            .clickMovingBackwards()
            .clickOnString(org.odk.collect.strings.R.string.yes)

            // after disabling moving backward - the state of the 4 related options is reversed
            .assertGoToPromptDisabled()
            .assertGoToPromptUnchecked()

            .assertSaveAsDraftInFormEntryDisabled()
            .assertSaveAsDraftInFormEntryUnchecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndUnchecked()

            .assertFinalizeDisabled()
            .assertFinalizeChecked()
    }

    @Test
    fun whenMovingBackwardDisabledWithoutPreventingUsersFormBypassingIt_relatedOptionsShouldNotBeUpdated() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickMovingBackwards()
            .clickOnString(org.odk.collect.strings.R.string.no)
            .assertGoToPromptEnabled()
            .assertSaveAsDraftInFormEntryEnabled()
            .assertSaveAsDraftInFormEndEnabled()
            .assertFinalizeEnabled()
            .assertGoToPromptChecked()
            .assertSaveAsDraftInFormEntryChecked()
            .assertSaveAsDraftInFormEndChecked()
            .assertFinalizeChecked()
    }
}
