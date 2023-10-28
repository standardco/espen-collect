package org.espen.collect.android.support.rules

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Disables animations and sets long press timeout to 3 seconds in an attempt to avoid flakiness.
 */
class PrepDeviceForTestsRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                ANIMATIONS.forEach { executeShellCommand("settings put global $it 0") }
                executeShellCommand("settings put secure long_press_timeout 3000")
                base.evaluate()
            }
        }
    }

    private fun executeShellCommand(command: String) {
        UiDevice.getInstance(getInstrumentation()).executeShellCommand(command)
    }
}

private val ANIMATIONS: List<String> = listOf(
    "transition_animation_scale",
    "window_animation_scale",
    "animator_duration_scale"
)
