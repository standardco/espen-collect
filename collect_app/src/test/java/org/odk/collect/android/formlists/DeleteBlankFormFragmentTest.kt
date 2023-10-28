package org.espen.collect.android.formlists

import android.app.Application
import android.net.Uri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.espen.collect.android.R
import org.espen.collect.android.formlists.blankformlist.BlankFormListItem
import org.espen.collect.android.formlists.blankformlist.BlankFormListMenuProvider
import org.espen.collect.android.formlists.blankformlist.BlankFormListViewModel
import org.espen.collect.android.formlists.blankformlist.DeleteBlankFormFragment
import org.espen.collect.androidshared.ui.FragmentFactoryBuilder
import org.espen.collect.androidshared.ui.MultiSelectViewModel
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import org.odk.collect.testshared.ViewActions.clickOnItemWith
import org.odk.collect.testshared.ViewMatchers.recyclerView

@RunWith(AndroidJUnit4::class)
class DeleteBlankFormFragmentTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val menuHost = RecordingMenuHost()

    private val multiSelectViewModel = MultiSelectViewModel()

    private val formsToDisplay = MutableLiveData<List<BlankFormListItem>>(emptyList())
    private val blankFormListViewModel = mock<BlankFormListViewModel> {
        on { formsToDisplay } doReturn formsToDisplay
        on { isLoading } doReturn MutableLiveData()
        on { isOutOfSyncWithServer() } doReturn MutableLiveData()
    }

    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                BlankFormListViewModel::class.java -> blankFormListViewModel
                MultiSelectViewModel::class.java -> multiSelectViewModel
                else -> throw IllegalArgumentException()
            } as T
        }
    }

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(DeleteBlankFormFragment::class) {
                DeleteBlankFormFragment(viewModelFactory, menuHost)
            }.build()
    )

    @Test
    fun `selected forms are checked`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        formsToDisplay.value = listOf(
            blankFormListItem(databaseId = 1, formName = "Form 1"),
            blankFormListItem(databaseId = 2, formName = "Form 2")
        )

        multiSelectViewModel.select(2)

        onView(withRecyclerView(R.id.list).atPositionOnView(1, R.id.form_title)).check(matches(withText("Form 2")))
        onView(withRecyclerView(R.id.list).atPositionOnView(1, R.id.checkbox)).check(matches(isChecked()))
    }

    @Test
    fun `clicking forms selects them`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        formsToDisplay.value = listOf(
            blankFormListItem(databaseId = 1, formName = "Form 1"),
            blankFormListItem(databaseId = 2, formName = "Form 2"),
            blankFormListItem(databaseId = 3, formName = "Form 3")
        )

        onView(recyclerView()).perform(clickOnItemWith(withText("Form 1")))
        onView(recyclerView()).perform(clickOnItemWith(withText("Form 3")))

        assertThat(multiSelectViewModel.getSelected().value, equalTo(setOf<Long>(1, 3)))
    }

    @Test
    fun `clicking selected forms unselects them`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        formsToDisplay.value = listOf(
            blankFormListItem(databaseId = 1, formName = "Form 1"),
            blankFormListItem(databaseId = 2, formName = "Form 2")
        )

        onView(recyclerView()).perform(clickOnItemWith(withText("Form 1")))
        onView(recyclerView()).perform(clickOnItemWith(withText("Form 2")))

        onView(recyclerView()).perform(clickOnItemWith(withText("Form 2")))

        assertThat(multiSelectViewModel.getSelected().value, equalTo(setOf<Long>(1)))
    }

    @Test
    fun `clicking select all selects all forms`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        formsToDisplay.value = listOf(
            blankFormListItem(databaseId = 1, formName = "Form 1"),
            blankFormListItem(databaseId = 2, formName = "Form 2")
        )

        onView(withText(org.odk.collect.strings.R.string.select_all)).perform(click())

        assertThat(multiSelectViewModel.getSelected().value, equalTo(setOf<Long>(1, 2)))
    }

    @Test
    fun `can click select all after selecting some forms`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        formsToDisplay.value = listOf(
            blankFormListItem(databaseId = 1, formName = "Form 1"),
            blankFormListItem(databaseId = 2, formName = "Form 2")
        )

        multiSelectViewModel.select(1)
        onView(withText(org.odk.collect.strings.R.string.select_all)).perform(click())

        multiSelectViewModel.unselect(1)
        onView(withText(org.odk.collect.strings.R.string.select_all)).perform(click())

        assertThat(multiSelectViewModel.getSelected().value, equalTo(setOf<Long>(1, 2)))
    }

    @Test
    fun `clicking clear all selects no forms`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        formsToDisplay.value = listOf(
            blankFormListItem(databaseId = 1, formName = "Form 1"),
            blankFormListItem(databaseId = 2, formName = "Form 2")
        )

        onView(withText(org.odk.collect.strings.R.string.clear_all)).check(doesNotExist())
        onView(withText(org.odk.collect.strings.R.string.select_all)).perform(click())

        onView(withText(org.odk.collect.strings.R.string.select_all)).check(doesNotExist())
        onView(withText(org.odk.collect.strings.R.string.clear_all)).perform(click())

        assertThat(multiSelectViewModel.getSelected().value, equalTo(emptySet()))

        onView(withText(org.odk.collect.strings.R.string.select_all)).check(matches(isDisplayed()))
        onView(withText(org.odk.collect.strings.R.string.clear_all)).check(doesNotExist())
    }

    @Test
    fun `clicking delete selected and then accepting deletes selected forms`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        formsToDisplay.value = listOf(
            blankFormListItem(databaseId = 11, formName = "Form 1"),
            blankFormListItem(databaseId = 12, formName = "Form 2")
        )

        multiSelectViewModel.select(11)
        multiSelectViewModel.select(12)

        onView(withText(org.odk.collect.strings.R.string.delete_file)).perform(click())
        onView(withText(context.getString(org.odk.collect.strings.R.string.delete_confirm, 2)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(org.odk.collect.strings.R.string.delete_yes)).inRoot(isDialog()).perform(click())

        verify(blankFormListViewModel).deleteForms(11, 12)
    }

    @Test
    fun `clicking delete selected and then cancelling does nothing`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        formsToDisplay.value = listOf(
            blankFormListItem(databaseId = 11, formName = "Form 1"),
            blankFormListItem(databaseId = 12, formName = "Form 2")
        )

        multiSelectViewModel.select(11)
        multiSelectViewModel.select(12)

        onView(withText(org.odk.collect.strings.R.string.delete_file)).perform(click())
        onView(withText(context.getString(org.odk.collect.strings.R.string.delete_confirm, 2)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(org.odk.collect.strings.R.string.delete_no)).inRoot(isDialog()).perform(click())

        verify(blankFormListViewModel, never()).deleteForms(any())
    }

    @Test
    fun `clicking delete selected unselects forms`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        formsToDisplay.value = listOf(
            blankFormListItem(databaseId = 11, formName = "Form 1"),
            blankFormListItem(databaseId = 12, formName = "Form 2")
        )

        multiSelectViewModel.select(11)

        onView(withText(org.odk.collect.strings.R.string.delete_file)).perform(click())
        onView(withText(context.getString(org.odk.collect.strings.R.string.delete_confirm, 1)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(org.odk.collect.strings.R.string.delete_yes)).inRoot(isDialog()).perform(click())

        assertThat(multiSelectViewModel.getSelected().value, equalTo(emptySet()))
    }

    @Test
    fun `delete selected is disabled and enabled when forms are selected or not`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)

        onView(withText(org.odk.collect.strings.R.string.delete_file)).check(matches(not(isEnabled())))

        multiSelectViewModel.select(11)
        onView(withText(org.odk.collect.strings.R.string.delete_file)).check(matches(isEnabled()))

        multiSelectViewModel.unselectAll()
        onView(withText(org.odk.collect.strings.R.string.delete_file)).check(matches(not(isEnabled())))
    }

    @Test
    fun `empty message shows when there are no forms`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)

        onView(withText(org.odk.collect.strings.R.string.no_items_display)).check(matches(isDisplayed()))

        formsToDisplay.value = listOf(blankFormListItem(databaseId = 1, formName = "Form 1"))

        onView(withText(org.odk.collect.strings.R.string.no_items_display)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `bottom buttons are hidden when there are no forms`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)
        onView(withId(R.id.buttons)).check(matches(not(isDisplayed())))

        formsToDisplay.value = listOf(blankFormListItem(databaseId = 1, formName = "Form 1"))
        onView(withId(R.id.buttons)).check(matches(isDisplayed()))
    }

    @Test
    fun `provides blank form menu`() {
        fragmentScenarioLauncherRule.launchInContainer(DeleteBlankFormFragment::class.java)

        val menuProviders = menuHost.getMenuProviders()
        assertThat(menuProviders.size, equalTo(1))
        assertThat(menuProviders[0].first, equalTo(Lifecycle.State.RESUMED))
        assertThat(menuProviders[0].second, instanceOf(BlankFormListMenuProvider::class.java))
    }

    private fun blankFormListItem(databaseId: Long = 1, formName: String = "Form 1") =
        BlankFormListItem(
            databaseId = databaseId,
            formId = "formId",
            formName = formName,
            formVersion = "formVersion",
            geometryPath = "",
            dateOfCreation = 0,
            dateOfLastUsage = 0,
            dateOfLastDetectedAttachmentsUpdate = null,
            Uri.parse("")
        )
}

private class RecordingMenuHost : MenuHost {

    private val menuProviders = mutableListOf<Pair<Lifecycle.State?, MenuProvider>>()

    override fun addMenuProvider(menuProvider: MenuProvider) {
        menuProviders.add(Pair(null, menuProvider))
    }

    override fun addMenuProvider(menuProvider: MenuProvider, lifecycleOwner: LifecycleOwner) {
        menuProviders.add(Pair(null, menuProvider))
    }

    override fun addMenuProvider(
        menuProvider: MenuProvider,
        lifecycleOwner: LifecycleOwner,
        state: Lifecycle.State
    ) {
        menuProviders.add(Pair(state, menuProvider))
    }

    override fun removeMenuProvider(menuProvider: MenuProvider) {
        TODO()
    }

    override fun invalidateMenu() { }

    fun getMenuProviders(): List<Pair<Lifecycle.State?, MenuProvider>> {
        return menuProviders
    }
}
