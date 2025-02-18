package org.odk.collect.android.support.rules

import androidx.test.espresso.NoMatchingRootException
import androidx.test.espresso.PerformException
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber

class RetryOnDeviceErrorRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    base.evaluate()
                } catch (e: Exception) {
                    if (e is PerformException) {
                        Timber.w("RetryOnDeviceErrorRule: Retrying due to PerformException!")
                        Timber.e(e)
                        base.evaluate()
                    } else if (e is NoMatchingRootException) {
                        Timber.w("RetryOnDeviceErrorRule: Retrying due to NoMatchingRootException!")
                        Timber.e(e)
                        base.evaluate()
                    } else if (e::class.simpleName == "RootViewWithoutFocusException") {
                        Timber.w("RetryOnDeviceErrorRule: Retrying due to RootViewWithoutFocusException!")
                        Timber.e(e)
                        base.evaluate()
                    } else {
                        throw e
                    }
                }
            }
        }
    }
}
