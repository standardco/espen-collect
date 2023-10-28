package org.espen.collect.android.support.rules

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.espen.collect.android.database.DatabaseConnection.Companion.closeAll
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.injection.config.AppDependencyComponent
import org.espen.collect.android.injection.config.AppDependencyModule
import org.espen.collect.android.support.CollectHelpers
import org.espen.collect.android.views.DecoratedBarcodeView
import org.espen.collect.androidshared.data.getState
import org.espen.collect.androidshared.ui.ToastUtils
import org.espen.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.material.BottomSheetBehavior
import org.odk.collect.shared.files.DirectoryUtils
import java.io.File
import java.io.IOException

private class ResetStateStatement(
    private val base: Statement,
    private val appDependencyModule: org.espen.collect.android.injection.config.AppDependencyModule? = null
) : Statement() {

    override fun evaluate() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val oldComponent = org.espen.collect.android.injection.DaggerUtils.getComponent(application)

        clearPrefs(oldComponent)
        clearDisk(oldComponent)
        setTestState()
        CollectHelpers.simulateProcessRestart(appDependencyModule)
        base.evaluate()
    }

    private fun clearAppState(application: Application) {
        application.getState().clear()
    }

    private fun setTestState() {
        MultiClickGuard.test = true
        DecoratedBarcodeView.test = true
        ToastUtils.recordToasts = true
        BottomSheetBehavior.DRAGGING_ENABLED = false
    }

    private fun clearDisk(component: org.espen.collect.android.injection.config.AppDependencyComponent) {
        try {
            DirectoryUtils.deleteDirectory(File(component.storagePathProvider().odkRootDirPath))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        closeAll()
    }

    private fun clearPrefs(component: org.espen.collect.android.injection.config.AppDependencyComponent) {
        val projectIds = component.projectsRepository().getAll().map { it.uuid }
        component.settingsProvider().clearAll(projectIds)
    }
}

class ResetStateRule @JvmOverloads constructor(
    private val appDependencyModule: org.espen.collect.android.injection.config.AppDependencyModule? = null
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement =
        ResetStateStatement(base, appDependencyModule)
}
