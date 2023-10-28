package org.espen.collect.android.support.rules

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.ExternalResource
import org.espen.collect.android.activities.FormFillingActivity
import org.espen.collect.android.external.FormsContract
import org.espen.collect.android.formmanagement.FormFillingIntentFactory
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.storage.StorageSubdirectory
import org.espen.collect.android.support.CollectHelpers
import org.espen.collect.android.support.StorageUtils
import org.espen.collect.android.support.pages.FormEntryPage
import org.espen.collect.android.support.pages.FormHierarchyPage
import org.espen.collect.android.support.pages.Page
import org.espen.collect.androidtest.ActivityScenarioExtensions.saveInstanceState
import timber.log.Timber
import java.io.IOException

class FormEntryActivityTestRule : ExternalResource() {

    private lateinit var intent: Intent
    private lateinit var scenario: ActivityScenario<Activity>

    override fun after() {
        try {
            scenario.close()
        } catch (e: Throwable) {
            Timber.e(Error("Error closing ActivityScenario: $e"))
        }
    }

    fun setUpProjectAndCopyForm(formFilename: String): FormEntryActivityTestRule {
        try {
            // Set up demo project
            CollectHelpers.addDemoProject()
            StorageUtils.copyFormToDemoProject(formFilename, null, true)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return this
    }

    fun <D : Page<D>> fillNewForm(formFilename: String, destination: D): D {
        intent = createNewFormIntent(formFilename)
        scenario = ActivityScenario.launch(intent)
        return destination.assertOnPage()
    }

    fun fillNewForm(formFilename: String, formName: String): FormEntryPage {
        return fillNewForm(formFilename, FormEntryPage(formName))
    }

    fun editForm(formFilename: String, instanceName: String): FormHierarchyPage {
        intent = createEditFormIntent(formFilename)
        scenario = ActivityScenario.launch(intent)
        return FormHierarchyPage(instanceName).assertOnPage()
    }

    fun navigateAwayFromActivity(): FormEntryActivityTestRule {
        scenario.moveToState(Lifecycle.State.STARTED)
        scenario.saveInstanceState()
        return this
    }

    fun destroyActivity(): FormEntryActivityTestRule {
        scenario.moveToState(Lifecycle.State.DESTROYED)
        return this
    }

    fun simulateProcessRestart(): FormEntryActivityTestRule {
        CollectHelpers.simulateProcessRestart()
        return this
    }

    private fun createNewFormIntent(formFilename: String): Intent {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val formPath = org.espen.collect.android.injection.DaggerUtils.getComponent(application).storagePathProvider()
            .getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.FORMS) + "/" + formFilename
        val form = org.espen.collect.android.injection.DaggerUtils.getComponent(application).formsRepositoryProvider().get()
            .getOneByPath(formPath)
        val projectId = org.espen.collect.android.injection.DaggerUtils.getComponent(application).currentProjectProvider()
            .getCurrentProject().uuid

        return FormFillingIntentFactory.newInstanceIntent(
            application,
            org.espen.collect.android.external.FormsContract.getUri(projectId, form!!.dbId),
            org.espen.collect.android.activities.FormFillingActivity::class
        )
    }

    private fun createEditFormIntent(formFilename: String): Intent {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val formPath = org.espen.collect.android.injection.DaggerUtils.getComponent(application).storagePathProvider()
            .getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.FORMS) + "/" + formFilename
        val form = org.espen.collect.android.injection.DaggerUtils.getComponent(application).formsRepositoryProvider().get()
            .getOneByPath(formPath)
        val instance = org.espen.collect.android.injection.DaggerUtils.getComponent(application).instancesRepositoryProvider().get()
            .getAllByFormId(form!!.formId).first()
        val projectId = org.espen.collect.android.injection.DaggerUtils.getComponent(application).currentProjectProvider()
            .getCurrentProject().uuid

        return FormFillingIntentFactory.editInstanceIntent(
            application,
            projectId,
            instance.dbId,
            org.espen.collect.android.activities.FormFillingActivity::class
        )
    }
}
