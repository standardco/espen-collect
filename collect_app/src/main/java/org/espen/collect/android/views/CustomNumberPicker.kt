/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.espen.collect.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import org.espen.collect.android.widgets.utilities.QuestionFontSizeUtils

class CustomNumberPicker(context: Context, attrs: AttributeSet) : NumberPicker(context, attrs) {
    override fun addView(view: View) {
        super.addView(view)
        updateView(view)
    }

    override fun addView(view: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(view, index, params)
        updateView(view)
    }

    override fun addView(view: View, params: ViewGroup.LayoutParams) {
        super.addView(view, params)
        updateView(view)
    }

    private fun updateView(view: View) {
        if (view is EditText) {
            view.textSize = QuestionFontSizeUtils.getQuestionFontSize().toFloat()
        }
    }
}
