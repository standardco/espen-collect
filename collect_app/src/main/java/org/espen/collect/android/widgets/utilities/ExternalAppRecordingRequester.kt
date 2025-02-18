package org.odk.collect.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsProvider

class ExternalAppRecordingRequester(
    private val activity: Activity,
    private val intentLauncher: IntentLauncher,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val permissionsProvider: PermissionsProvider
) : RecordingRequester {

    override fun requestRecording(prompt: FormEntryPrompt) {
        permissionsProvider.requestRecordAudioPermission(
            activity,
            object : PermissionListener {
                override fun granted() {
                    val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                    intent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()
                    )
                    waitingForDataRegistry.waitForData(prompt.index)
                    intentLauncher.launchForResult(
                        activity,
                        intent,
                        ApplicationConstants.RequestCodes.AUDIO_CAPTURE
                    ) {
                        Toast.makeText(
                            activity,
                            activity.getString(
                                org.odk.collect.strings.R.string.activity_not_found,
                                activity.getString(org.odk.collect.strings.R.string.capture_audio)
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        waitingForDataRegistry.cancelWaitingForData()
                    }
                }
            }
        )
    }
}
