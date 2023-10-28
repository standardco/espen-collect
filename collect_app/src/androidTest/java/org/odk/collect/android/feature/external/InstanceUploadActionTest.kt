package org.espen.collect.android.feature.external

import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.espen.collect.android.instancemanagement.send.InstanceUploaderActivity
import org.espen.collect.android.support.pages.OkDialog
import org.espen.collect.android.support.rules.CollectTestRule
import org.espen.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class InstanceUploadActionTest {

    val collectTestRule = CollectTestRule()

    @get:Rule
    val rule: RuleChain = TestRuleChain.chain()
        .around(collectTestRule)

    @Test
    fun whenInstanceDoesNotExist_showsError() {
        val instanceIds = longArrayOf(11)
        instanceUploadAction(instanceIds)

        OkDialog()
            .assertOnPage()
            .assertText(org.odk.collect.strings.R.string.no_forms_uploaded)
    }

    private fun instanceUploadAction(instanceIds: LongArray) {
        /*
        This should really use `Intent(action: String)` but this seems to be broken right now:
        https://github.com/android/android-test/issues/496
         */
        val intent = Intent(getApplicationContext(), org.espen.collect.android.instancemanagement.send.InstanceUploaderActivity::class.java)
        intent.putExtra("instances", instanceIds)
        collectTestRule.launch<org.espen.collect.android.instancemanagement.send.InstanceUploaderActivity>(intent)
    }
}
