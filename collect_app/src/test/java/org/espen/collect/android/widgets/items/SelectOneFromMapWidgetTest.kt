package org.espen.collect.android.widgets.items

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.javarosa.core.model.data.SelectOneData
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.espen.collect.android.fakes.FakePermissionsProvider
import org.espen.collect.android.formentry.FormEntryViewModel
import org.espen.collect.android.formentry.questions.QuestionDetails
import org.espen.collect.android.injection.config.AppDependencyModule
import org.espen.collect.android.listeners.AdvanceToNextListener
import org.espen.collect.android.preferences.GuidanceHint
import org.espen.collect.android.storage.StoragePathProvider
import org.espen.collect.android.support.CollectHelpers
import org.espen.collect.android.support.MockFormEntryPromptBuilder
import org.espen.collect.android.support.WidgetTestActivity
import org.espen.collect.android.widgets.support.FormElementFixtures.selectChoice
import org.espen.collect.android.widgets.support.NoOpMapFragment
import org.espen.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener
import org.espen.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer
import org.espen.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.espen.collect.android.widgets.utilities.QuestionFontSizeUtils.FontSize
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class SelectOneFromMapWidgetTest {

    private val activityController = Robolectric.buildActivity(WidgetTestActivity::class.java)
    private val formEntryViewModel = mock<FormEntryViewModel>()

    private val permissionsProvider = FakePermissionsProvider().also {
        it.setPermissionGranted(true)
    }

    private val settingsProvider = InMemSettingsProvider().also {
        it.getUnprotectedSettings().save(ProjectKeys.KEY_FONT_SIZE, "12")
        it.getUnprotectedSettings().save(ProjectKeys.KEY_GUIDANCE_HINT, GuidanceHint.YES.toString())
    }

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesPermissionsProvider(permissionsChecker: PermissionsChecker): PermissionsProvider =
                permissionsProvider

            override fun providesSettingsProvider(context: Context): SettingsProvider =
                settingsProvider

            override fun providesMapFragmentFactory(settingsProvider: SettingsProvider?): MapFragmentFactory {
                return object : MapFragmentFactory {
                    override fun createMapFragment(): MapFragment {
                        return NoOpMapFragment()
                    }
                }
            }

            override fun providesReferenceLayerRepository(
                storagePathProvider: StoragePathProvider,
                settingsProvider: SettingsProvider
            ): ReferenceLayerRepository {
                return mock()
            }
        })
    }

    @Test
    fun `button uses app font size`() {
        val settings = settingsProvider.getUnprotectedSettings()
        val widget = SelectOneFromMapWidget(
            activityController.get(),
            QuestionDetails(promptWithAnswer(null)),
            false,
            mock()
        )

        assertThat(
            widget.binding.button.textSize.toInt(),
            equalTo(QuestionFontSizeUtils.getFontSize(settings, FontSize.BODY_LARGE))
        )
    }

    @Test
    fun `clicking button opens SelectOneFromMapDialogFragment`() {
        val activity = activityController.setup().get()
        activity.supportFragmentManager.fragmentFactory = FragmentFactoryBuilder().forClass(SelectOneFromMapDialogFragment::class.java) {
            SelectOneFromMapDialogFragment(object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return formEntryViewModel as T
                }
            })
        }.build()

        val prompt = promptWithAnswer(null)
        val widget =
            SelectOneFromMapWidget(activity, QuestionDetails(prompt), false, mock())
        whenever(formEntryViewModel.getQuestionPrompt(prompt.index)).doReturn(prompt)

        widget.binding.button.performClick()

        val fragment = getFragmentByClass(
            activityController.get().supportFragmentManager,
            SelectOneFromMapDialogFragment::class.java
        )
        assertThat(fragment, notNullValue())
        assertThat(
            fragment?.requireArguments()
                ?.getSerializable(SelectOneFromMapDialogFragment.ARG_FORM_INDEX),
            equalTo(prompt.index)
        )
    }

    @Test
    fun `clicking button when location permissions denied does nothing`() {
        val widget = SelectOneFromMapWidget(
            activityController.get(),
            QuestionDetails(promptWithAnswer(null)),
            false,
            mock()
        )

        permissionsProvider.setPermissionGranted(false)
        widget.binding.button.performClick()

        val fragment = getFragmentByClass(
            activityController.get().supportFragmentManager,
            SelectOneFromMapDialogFragment::class.java
        )
        assertThat(fragment, nullValue())
    }

    @Test
    fun `shows answer`() {
        val choices = listOf(selectChoice("a"), selectChoice("b"))
        val prompt = MockFormEntryPromptBuilder()
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A", choices[1] to "B"))
            .withAnswer(SelectOneData(choices[0].selection()))
            .build()

        val widget = SelectOneFromMapWidget(activityController.get(), QuestionDetails(prompt), false, mock())
        assertThat(widget.binding.answer.text, equalTo("A"))
    }

    @Test
    fun `answer uses app font size`() {
        val settings = settingsProvider.getUnprotectedSettings()
        val widget = SelectOneFromMapWidget(
            activityController.get(),
            QuestionDetails(promptWithAnswer(null)),
            false,
            mock()
        )

        assertThat(
            widget.binding.answer.textSize.toInt(),
            equalTo(QuestionFontSizeUtils.getFontSize(settings, FontSize.HEADLINE_6))
        )
    }

    @Test
    fun `prompt answer is returned from getAnswer`() {
        val selectChoice = selectChoice(value = "a")
        val answer = SelectOneData(selectChoice.selection())

        val widget = SelectOneFromMapWidget(
            activityController.get(),
            QuestionDetails(promptWithAnswer(answer)),
            false,
            mock()
        )
        assertThat(widget.answer, equalTo(answer))
    }

    @Test
    fun `clearAnswer removes answer`() {
        val selectChoice = selectChoice(value = "a")
        val answer = SelectOneData(selectChoice.selection())

        val widget = SelectOneFromMapWidget(
            activityController.get(),
            QuestionDetails(promptWithAnswer(answer)),
            false,
            mock()
        )
        widget.clearAnswer()
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun `clearAnswer calls value changed listener`() {
        val selectChoice = selectChoice(value = "a")
        val answer = SelectOneData(selectChoice.selection())

        val widget = SelectOneFromMapWidget(
            activityController.get(),
            QuestionDetails(promptWithAnswer(answer)),
            false,
            mock()
        )

        val mockValueChangedListener = mockValueChangedListener(widget)
        widget.clearAnswer()
        verify(mockValueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun `clearAnswer updates shown answer`() {
        val choices = listOf(selectChoice("a"), selectChoice("b"))
        val prompt = MockFormEntryPromptBuilder()
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A", choices[1] to "B"))
            .withAnswer(SelectOneData(choices[0].selection()))
            .build()

        val widget = SelectOneFromMapWidget(activityController.get(), QuestionDetails(prompt), false, mock())

        widget.clearAnswer()
        assertThat(widget.binding.answer.text, equalTo(""))
    }

    @Test
    fun `setData sets answer`() {
        val widget = SelectOneFromMapWidget(
            activityController.get(),
            QuestionDetails(promptWithAnswer(null)),
            false,
            mock()
        )

        val selectChoice = selectChoice(value = "a", index = 101)
        val answer = SelectOneData(selectChoice.selection())
        widget.setData(answer)
        assertThat(widget.answer, equalTo(answer))
    }

    @Test
    fun `setData updates shown answer`() {
        val choices = listOf(selectChoice("a"), selectChoice("b"))
        val prompt = MockFormEntryPromptBuilder()
            .withSelectChoices(choices)
            .withSelectChoiceText(
                mapOf(choices[0] to "A", choices[1] to "B")
            )
            .build()
        val widget = SelectOneFromMapWidget(activityController.get(), QuestionDetails(prompt), false, mock())

        widget.setData(SelectOneData(choices[1].selection()))
        assertThat(widget.binding.answer.text, equalTo("B"))
    }

    @Test
    fun `setData calls value change listener`() {
        val choices = listOf(selectChoice("a"))
        val prompt = MockFormEntryPromptBuilder()
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val widget = SelectOneFromMapWidget(activityController.get(), QuestionDetails(prompt), false, mock())

        val mockValueChangedListener = mockValueChangedListener(widget)
        widget.setData(SelectOneData(choices[0].selection()))
        verify(mockValueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun `setData answer is passed to SelectOneFromMapDialogFragment`() {
        val activity = activityController.setup().get()
        activity.supportFragmentManager.fragmentFactory = FragmentFactoryBuilder().forClass(SelectOneFromMapDialogFragment::class.java) {
            SelectOneFromMapDialogFragment(object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return formEntryViewModel as T
                }
            })
        }.build()

        val choices = listOf(selectChoice("a"), selectChoice("b"))
        val prompt = MockFormEntryPromptBuilder()
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A", choices[1] to "B"))
            .build()

        val widget =
            SelectOneFromMapWidget(activity, QuestionDetails(prompt), false, mock())
        widget.setData(SelectOneData(choices[1].selection()))

        whenever(formEntryViewModel.getQuestionPrompt(prompt.index)).doReturn(prompt)
        widget.binding.button.performClick()

        val fragment = getFragmentByClass(
            activityController.get().supportFragmentManager,
            SelectOneFromMapDialogFragment::class.java
        )
        assertThat(fragment, notNullValue())
        assertThat(
            fragment?.requireArguments()
                ?.getSerializable(SelectOneFromMapDialogFragment.ARG_SELECTED_INDEX),
            equalTo(choices[1].index)
        )
    }

    @Test
    fun `setData calls AdvanceToNextListener when the 'quick' appearance is used`() {
        val listener = mock<AdvanceToNextListener>()
        val widget = SelectOneFromMapWidget(
            activityController.get(),
            QuestionDetails(promptWithAnswer(null)),
            true,
            listener
        )

        val selectChoice = selectChoice(value = "a", index = 101)
        val answer = SelectOneData(selectChoice.selection())
        widget.setData(answer)
        verify(listener).advance()
    }

    @Test
    fun `setData does not call AdvanceToNextListener when the 'quick' appearance is not used`() {
        val listener = mock<AdvanceToNextListener>()
        val widget = SelectOneFromMapWidget(
            activityController.get(),
            QuestionDetails(promptWithAnswer(null)),
            false,
            listener
        )

        val selectChoice = selectChoice(value = "a", index = 101)
        val answer = SelectOneData(selectChoice.selection())
        widget.setData(answer)
        verifyNoInteractions(listener)
    }
}
