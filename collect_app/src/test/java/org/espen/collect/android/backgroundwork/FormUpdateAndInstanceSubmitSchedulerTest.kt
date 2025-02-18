package org.odk.collect.android.backgroundwork

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.enums.FormUpdateMode.MATCH_EXACTLY
import org.odk.collect.settings.enums.FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProjectKeys.KEY_FORM_UPDATE_MODE
import org.odk.collect.settings.keys.ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK

@RunWith(AndroidJUnit4::class)
class FormUpdateAndInstanceSubmitSchedulerTest {

    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }
    private val settingsProvider by lazy { TestSettingsProvider.getSettingsProvider() }
    private val scheduler = mock<Scheduler>()

    @Test
    fun `scheduleUpdates passes project id when scheduling previously downloaded only`() {
        val generalSettings = settingsProvider.getUnprotectedSettings("myProject")

        generalSettings.save(KEY_FORM_UPDATE_MODE, PREVIOUSLY_DOWNLOADED_ONLY.getValue(application))
        generalSettings.save(
            KEY_PERIODIC_FORM_UPDATES_CHECK,
            application.getString(org.odk.collect.strings.R.string.every_one_hour_value)
        )

        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleUpdates("myProject")
        verify(scheduler).networkDeferredRepeat(
            eq("serverPollingJob:myProject"),
            any<AutoUpdateTaskSpec>(),
            eq(3600000),
            eq(mapOf(TaskData.DATA_PROJECT_ID to "myProject"))
        )
    }

    @Test
    fun `cancelUpdates cancels auto update for project`() {
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelUpdates("myProject")
        verify(scheduler).cancelDeferred("serverPollingJob:myProject")
    }

    @Test
    fun `cancelUpdates cancels match exactly update for project`() {
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelUpdates("myProject")
        verify(scheduler).cancelDeferred("match_exactly:myProject")
    }

    @Test
    fun `scheduleUpdates passes project id when scheduling match exactly`() {
        val generalSettings = settingsProvider.getUnprotectedSettings("myProject")

        generalSettings.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(application))
        generalSettings.save(
            KEY_PERIODIC_FORM_UPDATES_CHECK,
            application.getString(org.odk.collect.strings.R.string.every_one_hour_value)
        )

        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleUpdates("myProject")
        verify(scheduler).networkDeferredRepeat(
            eq("match_exactly:myProject"),
            any<SyncFormsTaskSpec>(),
            eq(3600000),
            eq(mapOf(TaskData.DATA_PROJECT_ID to "myProject"))
        )
    }

    @Test
    fun `scheduleAutoSend passes current project ID`() {
        settingsProvider.getUnprotectedSettings("myProject")
            .save(ProjectKeys.KEY_AUTOSEND, "wifi_and_cellular")
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleAutoSend("myProject")
        verify(scheduler).networkDeferred(
            eq("AutoSendWorker:myProject"),
            any<SendFormsTaskSpec>(),
            eq(mapOf(TaskData.DATA_PROJECT_ID to "myProject")),
            eq(null)
        )
    }

    @Test
    fun `scheduleAutoSend uses wifi network type when set in settings`() {
        settingsProvider.getUnprotectedSettings("myProject")
            .save(ProjectKeys.KEY_AUTOSEND, "wifi_only")
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleAutoSend("myProject")
        verify(scheduler).networkDeferred(
            eq("AutoSendWorker:myProject"),
            any<SendFormsTaskSpec>(),
            eq(mapOf(TaskData.DATA_PROJECT_ID to "myProject")),
            eq(Scheduler.NetworkType.WIFI)
        )
    }

    @Test
    fun `scheduleAutoSend uses cellular network type when set in settings`() {
        settingsProvider.getUnprotectedSettings("myProject")
            .save(ProjectKeys.KEY_AUTOSEND, "cellular_only")
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleAutoSend("myProject")
        verify(scheduler).networkDeferred(
            eq("AutoSendWorker:myProject"),
            any<SendFormsTaskSpec>(),
            eq(mapOf(TaskData.DATA_PROJECT_ID to "myProject")),
            eq(Scheduler.NetworkType.CELLULAR)
        )
    }

    @Test
    fun `scheduleAutoSend does nothing if auto send is disabled`() {
        settingsProvider.getUnprotectedSettings("myProject")
            .save(ProjectKeys.KEY_AUTOSEND, "off")
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.scheduleAutoSend("myProject")
        verifyNoInteractions(scheduler)
    }

    @Test
    fun `cancelSubmit cancels auto send for current project`() {
        val manager = FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application)

        manager.cancelSubmit("myProject")
        verify(scheduler).cancelDeferred("AutoSendWorker:myProject")
    }
}
