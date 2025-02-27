package org.espen.collect.android.support

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.espen.collect.android.application.Collect
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.injection.config.AppDependencyComponent
import org.espen.collect.android.injection.config.AppDependencyModule
import org.espen.collect.android.injection.config.DaggerAppDependencyComponent
import org.odk.collect.projects.Project

object CollectHelpers {
    fun overrideAppDependencyModule(appDependencyModule: AppDependencyModule): AppDependencyComponent {
        val application = ApplicationProvider.getApplicationContext<Collect>()
        val testComponent = DaggerAppDependencyComponent.builder()
            .application(application)
            .appDependencyModule(appDependencyModule)
            .build()
        application.component = testComponent
        return testComponent
    }

    fun simulateProcessRestart(appDependencyModule: AppDependencyModule? = null) {
        ApplicationProvider.getApplicationContext<Collect>().getState().clear()

        val newComponent =
            overrideAppDependencyModule(appDependencyModule ?: AppDependencyModule())

        // Reinitialize any application state with new deps/state
        newComponent.applicationInitializer().initialize()
    }

    @JvmStatic
    fun addDemoProject() {
        val component =
            DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
        component.projectsRepository().save(Project.DEMO_PROJECT)
        component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID)
    }
}
