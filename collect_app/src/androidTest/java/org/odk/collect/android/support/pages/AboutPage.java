package org.espen.collect.android.support.pages;

import org.espen.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class AboutPage extends Page<AboutPage> {

    @Override
    public AboutPage assertOnPage() {
        assertText(org.odk.collect.strings.R.string.about_preferences);
        return this;
    }

    public AboutPage scrollToOpenSourceLibrariesLicenses() {
        onView(withId(R.id.recyclerView)).perform(scrollToPosition(4));
        return this;
    }

    public OpenSourceLicensesPage clickOnOpenSourceLibrariesLicenses() {
        onView(withText(org.odk.collect.strings.R.string.all_open_source_licenses)).perform(click());
        return new OpenSourceLicensesPage();
    }
}
