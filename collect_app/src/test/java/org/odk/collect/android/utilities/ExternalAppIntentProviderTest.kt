package org.espen.collect.android.utilities

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class ExternalAppIntentProviderTest {
    private lateinit var formEntryPrompt: FormEntryPrompt
    private lateinit var externalAppIntentProvider: org.espen.collect.android.utilities.ExternalAppIntentProvider

    @Before
    fun setup() {
        formEntryPrompt = mock()
        externalAppIntentProvider = org.espen.collect.android.utilities.ExternalAppIntentProvider()
        `when`(formEntryPrompt.index).thenReturn(mock())
    }

    @Test
    fun intentAction_shouldBeSetProperly() {
        `when`(formEntryPrompt.appearanceHint).thenReturn("ex:com.example.collectanswersprovider()")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.action, `is`("com.example.collectanswersprovider"))
    }

    @Test
    fun whenNoParamsSpecified_shouldIntentHaveNoExtras() {
        `when`(formEntryPrompt.appearanceHint).thenReturn("ex:com.example.collectanswersprovider()")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.extras, nullValue())
    }

    @Test
    fun whenParamsSpecified_shouldIntentHaveExtras() {
        `when`(formEntryPrompt.appearanceHint)
            .thenReturn("ex:com.example.collectanswersprovider(param1='value1', param2='value2')")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.extras!!.keySet().size, `is`(2))
        assertThat(resultIntent.extras!!.getString("param1"), `is`("value1"))
        assertThat(resultIntent.extras!!.getString("param2"), `is`("value2"))
    }

    @Test
    fun whenParamsContainUri_shouldThatUriBeAddedAsIntentData() {
        `when`(formEntryPrompt.appearanceHint)
            .thenReturn("ex:com.example.collectanswersprovider(param1='value1', uri_data='file:///tmp/android.txt')")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.data.toString(), `is`("file:///tmp/android.txt"))
        assertThat(resultIntent.extras!!.keySet().size, `is`(1))
        assertThat(resultIntent.extras!!.getString("param1"), `is`("value1"))
    }
}
