package org.odk.collect.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.androidshared.system.IntentLauncher

class GetContentAudioFileRequester(
    private val activity: Activity,
    private val intentLauncher: IntentLauncher,
    private val waitingForDataRegistry: WaitingForDataRegistry
) : AudioFileRequester {

    override fun requestFile(prompt: FormEntryPrompt) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        waitingForDataRegistry.waitForData(prompt.index)
        intentLauncher.launchForResult(
            activity,
            intent,
            ApplicationConstants.RequestCodes.AUDIO_CHOOSER
        ) {
            Toast.makeText(
                activity,
                activity.getString(
                    org.odk.collect.strings.R.string.activity_not_found,
                    activity.getString(org.odk.collect.strings.R.string.choose_sound)
                ),
                Toast.LENGTH_SHORT
            ).show()
            waitingForDataRegistry.cancelWaitingForData()
        }
    }
}
