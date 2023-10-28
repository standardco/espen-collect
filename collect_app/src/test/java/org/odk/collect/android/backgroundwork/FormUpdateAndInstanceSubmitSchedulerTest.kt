package org.espen.collect.android.backgroundwork

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.espen.collect.android.TestSettingsProvider
import org.espen.collect.android.preferences.utilities.FormUpdateMode.MATCH_EXACTLY
import org.espen.collect.android.preferences.utilities.FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProjectKeys.KEY_FORM_UPDATE_MODE
import org.odk.collect.settings.keys.ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK
import org.odk.collect.settings.keys.ProjectKeys.KEY_PROTOCOL

@RunWith(AndroidJUnit4::class)
class FormUpdateAndInstanceSubmitSchedulerTest {

    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }
    private val settingsProvider by lazy { TestSettingsProvider.getSettingsProvider() }
    private val scheduler = mock<Scheduler>()

    @Test
    fun `scheduleUpdates passes project id when scheduling previously downloaded only`() {
        val generalSettings = settingsProvider.getUnprotectedSettings("myProject")

        generalSettings.save(KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)
        generalSettings.save(KEY_FORM_UPDATE_MODE, PREVIOUSLY_DOWNLOADED_ONLY.getValue(application))
        generalSettings.save(
            KEY_PERIODIC_FORM_UPDATES_CHECK,
            application.getString(org.odk.collect.strings.R.string.every_one_hour_value)
        )

        val manager = org.espen.collect.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleUpdates("myProject")
        verify(scheduler).networkDeferred(
            eq("serverPollingJob:myProject"),
            any<AutoUpdateTaskSpec>(),
            eq(3600000),
            eq(mapOf(TaskData.DATA_PROJECT_ID to "myProject"))
        )
    }

    @Test
    fun `cancelUpdates cancels auto update for project`() {
        val manager = org.espen.collect.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelUpdates("myProject")
        verify(scheduler).cancelDeferred("serverPollingJob:myProject")
    }

    @Test
    fun `cancelUpdates cancels match exactly update for project`() {
        val manager = org.espen.collect.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelUpdates("myProject")
        verify(scheduler).cancelDeferred("match_exactly:myProject")
    }

    @Test
    fun `scheduleUpdates passes project id when scheduling match exactly`() {
        val generalSettings = settingsProvider.getUnprotectedSettings("myProject")

        generalSettings.save(KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)
        generalSettings.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(application))
        generalSettings.save(
            KEY_PERIODIC_FORM_UPDATES_CHECK,
            application.getString(org.odk.collect.strings.R.string.every_one_hour_value)
        )

        val manager = org.espen.collect.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleUpdates("myProject")
        verify(scheduler).networkDeferred(
            eq("match_exactly:myProject"),
            any<SyncFormsTaskSpec>(),
            eq(3600000),
            eq(mapOf(TaskData.DATA_PROJECT_ID to "myProject"))
        )
    }

    @Test
    fun `scheduleSubmit passes current project ID`() {
        val manager = org.espen.collect.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleSubmit("myProject")
        verify(scheduler).networkDeferred(
            eq("AutoSendWorker:myProject"),
            any<AutoSendTaskSpec>(),
            eq(mapOf(TaskData.DATA_PROJECT_ID to "myProject"))
        )
    }

    @Test
    fun `cancelSubmit cancels auto send for current project`() {
        val manager = org.espen.collect.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelSubmit("myProject")
        verify(scheduler).cancelDeferred("AutoSendWorker:myProject")
    }
}
