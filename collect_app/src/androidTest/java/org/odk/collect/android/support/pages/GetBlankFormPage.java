package org.espen.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class GetBlankFormPage extends Page<GetBlankFormPage> {

    @Override
    public GetBlankFormPage assertOnPage() {
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.get_forms))).check(matches(isDisplayed()));
        return this;
    }

    public FormsDownloadResultPage clickGetSelected() {
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.download))).perform(click());
        return new FormsDownloadResultPage().assertOnPage();
    }
}
