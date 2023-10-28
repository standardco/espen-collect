package org.espen.collect.android.mainmenu

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.espen.collect.android.fakes.FakePermissionsProvider
import org.espen.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule

@RunWith(AndroidJUnit4::class)
class PermissionsDialogFragmentTest {

    private val permissionsProvider = FakePermissionsProvider()
    private val requestPermissionsViewModel = mock<RequestPermissionsViewModel>() {
        on { permissions } doReturn arrayOf("blah")
        on { shouldAskForPermissions() } doReturn true
    }

    private val fragmentFactory = FragmentFactoryBuilder()
        .forClass(PermissionsDialogFragment::class) {
            PermissionsDialogFragment(permissionsProvider, requestPermissionsViewModel)
        }
        .build()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(fragmentFactory)

    @Test
    fun clickingOK_asksForPermissions() {
        launcherRule.launch(PermissionsDialogFragment::class.java)

        onView(withText(org.odk.collect.strings.R.string.ok)).inRoot(isDialog()).perform(click())
        assertThat(
            permissionsProvider.requestedPermissions,
            equalTo(listOf("blah"))
        )
    }

    @Test
    fun clickingOK_callsPermissionRequested() {
        launcherRule.launch(PermissionsDialogFragment::class.java)

        onView(withText(org.odk.collect.strings.R.string.ok)).inRoot(isDialog()).perform(click())
        verify(requestPermissionsViewModel).permissionsRequested()
    }

    @Test
    fun dismissing_callsPermissionRequested() {
        launcherRule.launch(PermissionsDialogFragment::class.java)

        Espresso.pressBack()
        verify(requestPermissionsViewModel).permissionsRequested()
    }

    @Test
    fun recreating_doesNotCallPermissionsRequested() {
        launcherRule.launch(PermissionsDialogFragment::class.java).recreate()
        verify(requestPermissionsViewModel, never()).permissionsRequested()
    }
}
