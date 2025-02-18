package org.odk.collect.androidtest

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.test.core.app.ActivityScenario

object ActivityScenarioExtensions {

    /**
     * Calling finish() doesn't seem to move an Activity to the DESTROYED state when using
     * ActivityScenario but `isFinishing` appears to work correctly. Bug for this is tracked
     * at https://github.com/android/android-test/issues/978.
     */
    @JvmStatic
    val <T : Activity> ActivityScenario<T>.isFinishing: Boolean
        get() {
            var isFinishing = false
            this.onActivity {
                isFinishing = it.isFinishing
            }

            return isFinishing
        }

    fun <T : Activity> ActivityScenario<T>.saveInstanceState(): Bundle {
        val bundle = Bundle()
        onActivity { it.onSaveInstanceState(bundle, PersistableBundle()) }
        return bundle
    }
}
