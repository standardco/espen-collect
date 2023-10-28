package org.espen.collect.android.feature.formentry.backgroundlocation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ApplicationProvider;

import org.espen.collect.android.formentry.FormEntryMenuDelegate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.espen.collect.android.support.rules.BlankFormTestRule;
import org.espen.collect.android.support.rules.TestRuleChain;

public class SetGeopointActionTest {
    private static final String SETGEOPOINT_ACTION_FORM = "setgeopoint-action.xml";

    public BlankFormTestRule rule = new BlankFormTestRule(SETGEOPOINT_ACTION_FORM, "setgeopoint-action-instance-load");

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void locationCollectionSnackbar_ShouldBeDisplayedAtFormLaunch() {
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(String.format(ApplicationProvider.getApplicationContext().getString(org.odk.collect.strings.R.string.background_location_enabled), "⋮"))));
    }

    /**
     * Could be replaced in test for {@link FormEntryMenuDelegate}
     */
    @Test
    public void locationCollectionToggle_ShouldBeAvailable() {
        rule.startInFormEntry()
                .clickOptionsIcon()
                .assertText(org.odk.collect.strings.R.string.track_location);
    }
}
