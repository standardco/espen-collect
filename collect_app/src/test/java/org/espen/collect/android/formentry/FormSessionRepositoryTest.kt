package org.odk.collect.android.formentry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test
import org.mockito.kotlin.mock
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.forms.Form

abstract class FormSessionRepositoryTest {

    abstract val formSessionRepository: FormSessionRepository

    @Test
    fun create_returnsNewUniqueId() {
        val id1 = formSessionRepository.create()
        val id2 = formSessionRepository.create()

        assertThat(id1, not(equalTo(id2)))
    }

    @Test
    fun get_beforeSet_returnsEmptyLiveData() {
        val id = formSessionRepository.create()
        assertThat(formSessionRepository.get(id).getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun set_setsSessionForId() {
        val id1 = formSessionRepository.create()
        val id2 = formSessionRepository.create()

        val formController1 = mock<FormController>()
        val form1 = mock<Form>()

        val formController2 = mock<FormController>()
        val form2 = mock<Form>()

        formSessionRepository.set(id1, formController1, form1)
        formSessionRepository.set(id2, formController2, form2)

        assertThat(formSessionRepository.get(id1).getOrAwaitValue(), equalTo(FormSession(formController1, form1)))
        assertThat(formSessionRepository.get(id2).getOrAwaitValue(), equalTo(FormSession(formController2, form2)))
    }

    @Test
    fun clear_clearsLiveDataForId() {
        val id = formSessionRepository.create()
        val formController = mock<FormController>()
        val form = mock<Form>()
        formSessionRepository.set(id, formController, form)
        val liveData = formSessionRepository.get(id)

        formSessionRepository.clear(id)
        val newLiveData = formSessionRepository.get(id)

        assertThat(liveData.getOrAwaitValue(), equalTo(null))
        assertThat(liveData != newLiveData, equalTo(true))
    }
}
