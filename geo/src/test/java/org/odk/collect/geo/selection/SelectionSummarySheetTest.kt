package org.odk.collect.geo.selection

import android.app.Application
import android.view.View
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.geo.databinding.PropertyBinding
import org.odk.collect.geo.support.Fixtures
import org.odk.collect.testshared.RobolectricHelpers.getCreatedFromResId

@RunWith(AndroidJUnit4::class)
class SelectionSummarySheetTest {

    private val application = getApplicationContext<Application>().also {
        it.setTheme(com.google.android.material.R.style.Theme_Material3_Light)
    }

    @Test
    fun `setItem shows an error chip when the status is ERRORS`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectPoint().copy(
                status = Status.ERRORS
            )
        )

        assertThat(selectionSummarySheet.binding.statusChip.visibility, equalTo(View.VISIBLE))
        assertThat(selectionSummarySheet.binding.statusChip.errors, equalTo(true))
    }

    @Test
    fun `setItem shows a no-error chip when the status is NO_ERRORS`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectPoint().copy(
                status = Status.NO_ERRORS
            )
        )

        assertThat(selectionSummarySheet.binding.statusChip.visibility, equalTo(View.VISIBLE))
        assertThat(selectionSummarySheet.binding.statusChip.errors, equalTo(false))
    }

    @Test
    fun `setItem does not show a chip if the status is null`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(Fixtures.actionMappableSelectPoint())

        assertThat(selectionSummarySheet.binding.statusChip.visibility, equalTo(View.GONE))
    }

    @Test
    fun `setItem shows name`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectPoint().copy(
                name = "Cosmic Dread"
            )
        )

        assertThat(selectionSummarySheet.binding.name.text, equalTo("Cosmic Dread"))
    }

    @Test
    fun `setItem shows properties`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectPoint().copy(
                properties = listOf(
                    IconifiedText(
                        android.R.drawable.ic_btn_speak_now,
                        "Emotion"
                    ),
                    IconifiedText(
                        android.R.drawable.ic_dialog_info,
                        "Mystery"
                    )
                )
            )
        )

        val firstProperty =
            PropertyBinding.bind(selectionSummarySheet.binding.properties.getChildAt(0))
        assertThat(firstProperty.text.text, equalTo("Emotion"))
        val firstIcon = firstProperty.icon.drawable
        assertThat(getCreatedFromResId(firstIcon), equalTo(android.R.drawable.ic_btn_speak_now))

        val secondProperty =
            PropertyBinding.bind(selectionSummarySheet.binding.properties.getChildAt(1))
        assertThat(secondProperty.text.text, equalTo("Mystery"))
        val secondIcon = secondProperty.icon.drawable
        assertThat(getCreatedFromResId(secondIcon), equalTo(android.R.drawable.ic_dialog_info))
    }

    @Test
    fun `properties without icons have hidden icon view`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectPoint().copy(
                properties = listOf(
                    IconifiedText(
                        null,
                        "Emotion"
                    )
                )
            )
        )

        val property = PropertyBinding.bind(selectionSummarySheet.binding.properties.getChildAt(0))
        assertThat(property.text.text, equalTo("Emotion"))
        assertThat(property.icon.visibility, equalTo(View.GONE))
    }

    @Test
    fun `properties reset between items`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectPoint().copy(
                properties = listOf(
                    IconifiedText(
                        android.R.drawable.ic_btn_speak_now,
                        "Emotion"
                    )
                )
            )
        )

        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectPoint().copy(
                properties = listOf(
                    IconifiedText(
                        android.R.drawable.ic_dialog_info,
                        "Mystery"
                    )
                )
            )
        )

        assertThat(selectionSummarySheet.binding.properties.childCount, equalTo(1))

        val property =
            PropertyBinding.bind(selectionSummarySheet.binding.properties.getChildAt(0))
        assertThat(property.text.text, equalTo("Mystery"))
        val firstIcon = property.icon.drawable
        assertThat(getCreatedFromResId(firstIcon), equalTo(android.R.drawable.ic_dialog_info))
    }

    @Test
    fun `setItem shows info and hides action when it is non-null`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.infoMappableSelectPoint().copy(
                info = "Don't even bother looking"
            )
        )

        assertThat(selectionSummarySheet.binding.info.visibility, equalTo(View.VISIBLE))
        assertThat(selectionSummarySheet.binding.action.visibility, equalTo(View.GONE))
        assertThat(selectionSummarySheet.binding.info.text, equalTo("Don't even bother looking"))
    }

    @Test
    fun `setItem shows action and hides info when it is non-null`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectPoint().copy(
                action = IconifiedText(
                    android.R.drawable.ic_btn_speak_now,
                    "Come on in"
                )
            )
        )

        assertThat(selectionSummarySheet.binding.action.visibility, equalTo(View.VISIBLE))
        assertThat(selectionSummarySheet.binding.info.visibility, equalTo(View.GONE))

        assertThat(selectionSummarySheet.binding.action.text, equalTo("Come on in"))
        val iconDrawable = selectionSummarySheet.binding.action.icon
        assertThat(
            getCreatedFromResId(iconDrawable!!),
            equalTo(android.R.drawable.ic_btn_speak_now)
        )
    }
}
