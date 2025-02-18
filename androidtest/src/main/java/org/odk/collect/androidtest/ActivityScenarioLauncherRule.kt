package org.odk.collect.androidtest

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import org.junit.rules.ExternalResource
import timber.log.Timber

/**
 * Alternative to [ActivityScenario] that allows tests to do work before launching the [Activity]
 * (like switch out dependencies, construct intents etc) and also allows creation of multiple
 * scenarios in a test.
 */
open class ActivityScenarioLauncherRule : ExternalResource() {

    private val scenarios = mutableListOf<ActivityScenario<*>>()

    fun <A : Activity> launch(activityClass: Class<A>): ActivityScenario<A> {
        return ActivityScenario.launch(activityClass).also {
            scenarios.add(it)
        }
    }

    fun <A : Activity> launch(intent: Intent): ActivityScenario<A> {
        return ActivityScenario.launch<A>(intent).also {
            scenarios.add(it)
        }
    }

    fun <A : Activity> launchForResult(intent: Intent): ActivityScenario<A> {
        return ActivityScenario.launchActivityForResult<A>(intent).also {
            scenarios.add(it)
        }
    }

    fun <A : Activity> launchForResult(activityClass: Class<A>): ActivityScenario<A> {
        return ActivityScenario.launchActivityForResult(activityClass).also {
            scenarios.add(it)
        }
    }

    override fun after() {
        scenarios.forEach {
            try {
                it.close()
            } catch (e: Throwable) {
                Timber.e(Error("Error closing ActivityScenario: $e"))
            }
        }
    }
}
