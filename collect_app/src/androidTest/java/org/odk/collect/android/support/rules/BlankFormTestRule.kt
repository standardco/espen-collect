package org.espen.collect.android.support.rules

import android.app.Activity
import android.app.Application
import android.content.Intent
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
import timber.log.Timber
import java.io.IOException

class BlankFormTestRule @JvmOverloads constructor(
    private val formFilename: String,
    private val formName: String,
    private val mediaFilePaths: List<String>? = null
) : ExternalResource() {

    private lateinit var scenario: ActivityScenario<Activity>

    override fun before() {
        setUpProjectAndCopyForm()
        scenario = ActivityScenario.launch(activityIntent)
    }

    override fun after() {
        try {
            scenario.close()
        } catch (e: Throwable) {
            Timber.e(Error("Error closing ActivityScenario: $e"))
        }
    }

    fun startInFormEntry(): FormEntryPage {
        return FormEntryPage(formName).assertOnPage()
    }

    private fun setUpProjectAndCopyForm() {
        try {
            CollectHelpers.addDemoProject()
            StorageUtils.copyFormToDemoProject(formFilename, mediaFilePaths, true)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private val activityIntent: Intent
        get() {
            val application = ApplicationProvider.getApplicationContext<Application>()
            val formPath = org.espen.collect.android.injection.DaggerUtils.getComponent(application).storagePathProvider()
                .getOdkDirPath(org.espen.collect.android.storage.StorageSubdirectory.FORMS) + "/" + formFilename
            val form = org.espen.collect.android.injection.DaggerUtils.getComponent(application).formsRepositoryProvider().get()
                .getOneByPath(formPath)
            val projectId = org.espen.collect.android.injection.DaggerUtils.getComponent(application).currentProjectProvider()
                .getCurrentProject().uuid

            return FormFillingIntentFactory.newInstanceIntent(application, org.espen.collect.android.external.FormsContract.getUri(projectId, form!!.dbId), org.espen.collect.android.activities.FormFillingActivity::class)
        }
}
