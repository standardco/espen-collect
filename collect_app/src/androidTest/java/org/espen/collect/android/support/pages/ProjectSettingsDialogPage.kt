package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf
import org.odk.collect.testshared.WaitFor

internal class ProjectSettingsDialogPage : Page<ProjectSettingsDialogPage>() {

    override fun assertOnPage(): ProjectSettingsDialogPage {
        assertText(org.odk.collect.strings.R.string.projects)
        return this
    }

    fun clickSettings(): ProjectSettingsPage {
        return clickOnTextInDialog(
            org.odk.collect.strings.R.string.settings,
            ProjectSettingsPage()
        )
    }

    fun clickAbout(): AboutPage {
        return clickOnTextInDialog(
            org.odk.collect.strings.R.string.about_preferences,
            AboutPage()
        )
    }

    fun clickAddProject(): QrCodeProjectCreatorDialogPage {
        return clickOnTextInDialog(
            org.odk.collect.strings.R.string.add_project,
            QrCodeProjectCreatorDialogPage()
        )
    }

    fun assertCurrentProject(projectName: String, subtext: String): ProjectSettingsDialogPage {
        onView(
            allOf(
                hasDescendant(withText(projectName)),
                hasDescendant(withText(subtext)),
                withContentDescription(
                    getTranslatedString(
                        org.odk.collect.strings.R.string.using_project,
                        projectName
                    )
                )
            )
        ).check(matches(isDisplayed()))
        return this
    }

    fun assertInactiveProject(projectName: String, subtext: String): ProjectSettingsDialogPage {
        onView(
            allOf(
                hasDescendant(withText(projectName)),
                hasDescendant(withText(subtext)),
                withContentDescription(
                    getTranslatedString(
                        org.odk.collect.strings.R.string.switch_to_project,
                        projectName
                    )
                )
            )
        ).check(matches(isDisplayed()))
        return this
    }

    fun assertNotInactiveProject(projectName: String): ProjectSettingsDialogPage {
        onView(
            allOf(
                hasDescendant(withText(projectName)),
                withContentDescription(
                    getTranslatedString(
                        org.odk.collect.strings.R.string.switch_to_project,
                        projectName
                    )
                )
            )
        ).check(doesNotExist())
        return this
    }

    fun selectProject(projectName: String): MainMenuPage {
        WaitFor.wait250ms() // https://github.com/android/android-test/issues/444
        onView(
            allOf(
                hasDescendant(withText(projectName)),
                withContentDescription(
                    getTranslatedString(
                        org.odk.collect.strings.R.string.switch_to_project,
                        projectName
                    )
                )
            )
        )
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        return MainMenuPage().assertOnPage()
    }
}
