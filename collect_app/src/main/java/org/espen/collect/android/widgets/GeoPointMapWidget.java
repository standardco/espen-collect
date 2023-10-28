/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.espen.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import org.espen.collect.android.widgets.interfaces.GeoDataRequester;
import org.espen.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.espen.collect.android.widgets.utilities.GeoWidgetUtils;
import org.espen.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import org.espen.collect.android.databinding.GeoWidgetAnswerBinding;
import org.espen.collect.android.formentry.questions.QuestionDetails;
import org.espen.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.espen.collect.android.widgets.interfaces.GeoDataRequester;
import org.espen.collect.android.widgets.utilities.GeoWidgetUtils;
import org.espen.collect.android.widgets.utilities.WaitingForDataRegistry;

@SuppressLint("ViewConstructor")
public class GeoPointMapWidget extends QuestionWidget implements WidgetDataReceiver {
    GeoWidgetAnswerBinding binding;

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final GeoDataRequester geoDataRequester;

    private String answerText;

    public GeoPointMapWidget(Context context, QuestionDetails questionDetails,
                             WaitingForDataRegistry waitingForDataRegistry, GeoDataRequester geoDataRequester) {
        super(context, questionDetails);
        render();

        this.waitingForDataRegistry = waitingForDataRegistry;
        this.geoDataRequester = geoDataRequester;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize, int controlFontSize) {
        binding = GeoWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        binding.geoAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.simpleButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, controlFontSize);

        binding.simpleButton.setOnClickListener(v -> geoDataRequester.requestGeoPoint(prompt, answerText, waitingForDataRegistry));

        answerText = prompt.getAnswerText();
        String answerToDisplay = GeoWidgetUtils.getGeoPointAnswerToDisplay(getContext(), answerText);

        if (answerToDisplay.isEmpty()) {
            if (getFormEntryPrompt().isReadOnly()) {
                binding.simpleButton.setVisibility(GONE);
            } else {
                binding.simpleButton.setText(org.odk.collect.strings.R.string.get_point);
            }
            answerText = null;
        } else {
            if (getFormEntryPrompt().isReadOnly()) {
                binding.simpleButton.setText(org.odk.collect.strings.R.string.geopoint_view_read_only);
            } else {
                binding.simpleButton.setText(org.odk.collect.strings.R.string.view_change_location);
            }

            binding.geoAnswerText.setText(answerToDisplay);
        }

        return binding.getRoot();
    }

    @Override
    public IAnswerData getAnswer() {
        double[] parsedGeometryPoint = GeoWidgetUtils.parseGeometryPoint(answerText);
        return parsedGeometryPoint == null
                ? null
                : new GeoPointData(parsedGeometryPoint);
    }

    @Override
    public void clearAnswer() {
        answerText = null;
        binding.geoAnswerText.setText(null);
        binding.simpleButton.setText(org.odk.collect.strings.R.string.get_point);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.simpleButton.setOnLongClickListener(l);
        binding.geoAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.simpleButton.cancelLongPress();
        binding.geoAnswerText.cancelLongPress();
    }

    @Override
    public void setData(Object answer) {
        String answerToDisplay = GeoWidgetUtils.getGeoPointAnswerToDisplay(getContext(), answer.toString());
        if (answerToDisplay.isEmpty()) {
            answerText = null;
            binding.geoAnswerText.setText("");
            binding.simpleButton.setText(org.odk.collect.strings.R.string.get_point);
        } else {
            answerText = answer.toString();
            binding.geoAnswerText.setText(answerToDisplay);
            binding.simpleButton.setText(org.odk.collect.strings.R.string.view_change_location);
        }
        widgetValueChanged();
    }
}
