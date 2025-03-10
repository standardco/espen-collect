package org.espen.collect.android.feature.external

import android.content.Intent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.espen.collect.android.external.InstancesContract
import org.espen.collect.android.support.ContentProviderUtils
import org.espen.collect.android.support.pages.EditSavedFormPage
import org.espen.collect.android.support.rules.CollectTestRule
import org.espen.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class InstancePickActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun pickInstance_andTheSelectingInstance_returnsInstanceUri() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = InstancesContract.CONTENT_TYPE
        val result = rule.launchForResult(intent, EditSavedFormPage(true)) {
            it.clickOnFormClosingApp("One Question")
        }

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        assertThat(
            result.resultData.data,
            equalTo(InstancesContract.getUri("DEMO", instanceId))
        )
    }
}
