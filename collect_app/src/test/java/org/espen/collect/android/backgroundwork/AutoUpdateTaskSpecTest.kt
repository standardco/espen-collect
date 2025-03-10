package org.espen.collect.android.backgroundwork

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.espen.collect.android.formmanagement.FormsDataService
import org.espen.collect.android.injection.config.AppDependencyModule
import org.espen.collect.android.injection.config.ProjectDependencyModuleFactory
import org.espen.collect.android.notifications.Notifier
import org.espen.collect.android.support.CollectHelpers

@RunWith(AndroidJUnit4::class)
class AutoUpdateTaskSpecTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val formUpdateChecker = mock<FormsDataService>()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesFormsUpdater(
                application: Application,
                notifier: Notifier,
                projectDependencyModuleFactory: ProjectDependencyModuleFactory
            ): FormsDataService {
                return formUpdateChecker
            }
        })
    }

    @Test
    fun `calls checkForUpdates with project from tag`() {
        val autoUpdateTaskSpec = AutoUpdateTaskSpec()
        val task = autoUpdateTaskSpec.getTask(context, mapOf(TaskData.DATA_PROJECT_ID to "projectId"), true)

        task.get()
        verify(formUpdateChecker).downloadUpdates("projectId")
    }

    @Test
    fun `maxRetries should not be limited`() {
        assertThat(AutoUpdateTaskSpec().maxRetries, `is`(nullValue()))
    }
}
