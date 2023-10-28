package org.espen.collect.android.activities

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.espen.collect.android.external.FormsContract
import org.espen.collect.android.formmanagement.FormFillingIntentFactory
import org.espen.collect.android.injection.config.AppDependencyComponent
import org.espen.collect.android.injection.config.AppDependencyModule
import org.espen.collect.android.storage.StorageSubdirectory
import org.espen.collect.android.support.CollectHelpers
import org.espen.collect.android.support.CollectHelpers.resetProcess
import org.espen.collect.android.utilities.FileUtils
import org.espen.collect.androidshared.ui.DialogFragmentUtils
import org.espen.collect.androidtest.RecordedIntentsRule
import org.odk.collect.async.Scheduler
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.forms.Form
import org.odk.collect.formstest.FormFixtures.form
import org.odk.collect.strings.R
import org.odk.collect.testshared.ActivityControllerRule
import org.odk.collect.testshared.AssertIntentsHelper
import org.odk.collect.testshared.EspressoHelpers.assertText
import org.odk.collect.testshared.EspressoHelpers.clickOnContentDescription
import org.odk.collect.testshared.EspressoHelpers.clickOnText
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.RobolectricHelpers.recreateWithProcessRestore
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
class FormFillingActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val recordedIntentsRule = RecordedIntentsRule()

    @get:Rule
    val activityControllerRule = ActivityControllerRule()

    private val assertIntentsHelper = AssertIntentsHelper()

    private val scheduler = FakeScheduler()
    private val dependencies = object : org.espen.collect.android.injection.config.AppDependencyModule() {
        override fun providesScheduler(workManager: WorkManager): Scheduler {
            return scheduler
        }
    }

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var component: org.espen.collect.android.injection.config.AppDependencyComponent

    @Before
    fun setup() {
        component = CollectHelpers.overrideAppDependencyModule(dependencies)
    }

    @Test
    fun whenProcessIsKilledAndRestored_returnsToHierarchyAtQuestion() {
        val projectId = CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question.xml")
        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            org.espen.collect.android.external.FormsContract.getUri(projectId, form!!.dbId),
            org.espen.collect.android.activities.FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(org.espen.collect.android.activities.FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertText("Two Question")
        assertText("What is your name?")

        clickOnText(R.string.form_forward)
        scheduler.flush()
        assertText("What is your age?")

        // Recreate and assert we start FormHierarchyActivity
        val recreated = activityControllerRule.add {
            initial.recreateWithProcessRestore { resetProcess(dependencies) }
        }

        scheduler.flush()
        assertIntentsHelper.assertNewIntent(org.espen.collect.android.activities.FormHierarchyActivity::class)

        // Return to FormFillingActivity from FormHierarchyActivity
        val hierarchyIntent = shadowOf(recreated.get()).nextStartedActivityForResult.intent
        shadowOf(recreated.get()).receiveResult(hierarchyIntent, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertText("Two Question")
        assertText("What is your age?")
    }

    @Test
    fun whenProcessIsKilledAndRestored_andHierarchyIsOpen_returnsToHierarchyAtQuestion() {
        val projectId = CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question.xml")
        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            org.espen.collect.android.external.FormsContract.getUri(projectId, form!!.dbId),
            org.espen.collect.android.activities.FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(org.espen.collect.android.activities.FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertText("Two Question")
        assertText("What is your name?")

        clickOnText(R.string.form_forward)
        scheduler.flush()
        assertText("What is your age?")

        clickOnContentDescription(R.string.view_hierarchy)
        assertIntentsHelper.assertNewIntent(org.espen.collect.android.activities.FormHierarchyActivity::class)

        // Recreate and assert we start FormHierarchyActivity
        val recreated = activityControllerRule.add {
            initial.recreateWithProcessRestore { resetProcess(dependencies) }
        }

        scheduler.flush()
        assertIntentsHelper.assertNewIntent(org.espen.collect.android.activities.FormHierarchyActivity::class)

        // Return to FormFillingActivity from FormHierarchyActivity
        val hierarchyIntent = shadowOf(recreated.get()).nextStartedActivityForResult.intent
        shadowOf(recreated.get()).receiveResult(hierarchyIntent, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertText("Two Question")
        assertText("What is your age?")
    }

    @Test
    fun whenProcessIsKilledAndRestored_andThereADialogFragmentOpen_doesNotRestoreDialogFragment() {
        val projectId = CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question.xml")
        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            org.espen.collect.android.external.FormsContract.getUri(projectId, form!!.dbId),
            org.espen.collect.android.activities.FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(org.espen.collect.android.activities.FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertText("Two Question")
        assertText("What is your name?")

        clickOnText(R.string.form_forward)
        scheduler.flush()
        assertText("What is your age?")

        val initialFragmentManager = initial.get().supportFragmentManager
        DialogFragmentUtils.showIfNotShowing(TestDialogFragment::class.java, initialFragmentManager)
        assertThat(
            initialFragmentManager.fragments.any { it::class == TestDialogFragment::class },
            equalTo(true)
        )

        // Recreate and assert we start FormHierarchyActivity
        val recreated = activityControllerRule.add {
            initial.recreateWithProcessRestore { resetProcess(dependencies) }
        }

        scheduler.flush()
        assertIntentsHelper.assertNewIntent(org.espen.collect.android.activities.FormHierarchyActivity::class)

        // Return to FormFillingActivity from FormHierarchyActivity
        val hierarchyIntent = shadowOf(recreated.get()).nextStartedActivityForResult.intent
        shadowOf(recreated.get()).receiveResult(hierarchyIntent, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertText("Two Question")
        assertText("What is your age?")
    }

    @Test
    fun whenProcessIsKilledAndRestored_andIsWaitingForExternalData_dataCanStillBeReturned() {
        val projectId = CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question-external.xml")
        val intent = FormFillingIntentFactory.newInstanceIntent(
            application,
            org.espen.collect.android.external.FormsContract.getUri(projectId, form!!.dbId),
            org.espen.collect.android.activities.FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(org.espen.collect.android.activities.FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertText("Two Question")
        assertText("What is your name?")

        clickOnText(R.string.form_forward)
        scheduler.flush()
        assertText("What is your age?")

        // Open external app
        clickOnContentDescription(R.string.launch_app)
        assertIntentsHelper.assertNewIntent(hasAction("com.example.EXAMPLE"))

        // Recreate with result
        val returnData = ExternalAppUtils.getReturnIntent("159")
        activityControllerRule.add {
            initial.recreateWithProcessRestore(RESULT_OK, returnData) { resetProcess(dependencies) }
        }

        scheduler.flush()

        assertIntentsHelper.assertNoNewIntent()
        assertText("Two Question")
        assertText("What is your age?")
        assertText("159")
    }

    private fun setupForm(testFormPath: String): Form? {
        val formsDir = component.storagePathProvider().getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.FORMS)
        val formFile = org.espen.collect.android.utilities.FileUtils.copyFileFromResources(
            testFormPath,
            File(formsDir, "two-question.xml")
        )

        val formsRepository = component.formsRepositoryProvider().get()
        val form = formsRepository.save(form(formFile = formFile))
        return form
    }

    class TestDialogFragment : DialogFragment()
}
