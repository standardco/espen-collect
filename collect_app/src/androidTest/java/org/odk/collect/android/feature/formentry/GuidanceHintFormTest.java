package org.espen.collect.android.feature.formentry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.espen.collect.android.R;
import org.espen.collect.android.injection.DaggerUtils;
import org.espen.collect.android.preferences.GuidanceHint;
import org.espen.collect.android.support.rules.BlankFormTestRule;
import org.espen.collect.android.support.rules.TestRuleChain;
import org.odk.collect.settings.keys.ProjectKeys;

public class GuidanceHintFormTest {
    private static final String GUIDANCE_SAMPLE_FORM = "guidance_hint_form.xml";

    public BlankFormTestRule activityTestRule = new BlankFormTestRule(GUIDANCE_SAMPLE_FORM, "Guidance Form Sample");

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(activityTestRule);

    @Test
    public void guidanceHint_ShouldBeHiddenByDefault() {
        onView(ViewMatchers.withId(R.id.guidance_text_view)).check(matches(not(isDisplayed())));
    }

    @Test
    public void guidanceHint_ShouldBeDisplayedWhenSettingSetToYes() {
        DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext())
                .settingsProvider()
                .getUnprotectedSettings()
                .save(ProjectKeys.KEY_GUIDANCE_HINT, GuidanceHint.YES.toString());

        // jump to force recreation of the view after the settings change
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.jumpBeginningButton)).perform(click());

        onView(withId(R.id.guidance_text_view)).check(matches(withText("If the age is less than 18, the remainder of the survey will be hidden.")));
    }

    @Test
    public void guidanceHint_ShouldBeDisplayedAfterClickWhenSettingSetToYesCollapsed() {
        DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext())
                .settingsProvider()
                .getUnprotectedSettings()
                .save(ProjectKeys.KEY_GUIDANCE_HINT, GuidanceHint.YES_COLLAPSED.toString());

        // jump to force recreation of the view after the settings change
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.jumpBeginningButton)).perform(click());

        onView(withId(R.id.guidance_text_view)).check(matches(not(isDisplayed())));
        onView(withId(R.id.help_icon)).perform(click());
        onView(withId(R.id.guidance_text_view)).check(matches(withText("If the age is less than 18, the remainder of the survey will be hidden.")));
    }
}
