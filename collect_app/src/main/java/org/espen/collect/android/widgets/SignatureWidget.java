/*
 * Copyright (C) 2012 University of Washington
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

import static org.espen.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;
import static org.espen.collect.android.utilities.ApplicationConstants.RequestCodes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import org.espen.collect.android.draw.DrawActivity;
import org.espen.collect.android.utilities.ApplicationConstants;
import org.espen.collect.android.utilities.QuestionMediaManager;
import org.espen.collect.android.widgets.interfaces.ButtonClickListener;
import org.espen.collect.android.widgets.utilities.QuestionFontSizeUtils;
import org.espen.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.espen.collect.android.draw.DrawActivity;
import org.espen.collect.android.formentry.questions.QuestionDetails;
import org.espen.collect.android.formentry.questions.WidgetViewUtils;
import org.espen.collect.android.utilities.QuestionMediaManager;
import org.espen.collect.android.widgets.interfaces.ButtonClickListener;
import org.espen.collect.android.widgets.utilities.QuestionFontSizeUtils;
import org.espen.collect.android.widgets.utilities.WaitingForDataRegistry;

/**
 * Signature widget.
 *
 * @author BehrAtherton@gmail.com
 */
@SuppressLint("ViewConstructor")
public class SignatureWidget extends BaseImageWidget implements ButtonClickListener {

    Button signButton;

    public SignatureWidget(Context context, QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry, String tmpImageFilePath) {
        super(context, prompt, questionMediaManager, waitingForDataRegistry, tmpImageFilePath);
        render();

        imageClickHandler = new DrawImageClickHandler(DrawActivity.OPTION_SIGNATURE, ApplicationConstants.RequestCodes.SIGNATURE_CAPTURE, org.odk.collect.strings.R.string.signature_capture);
        setUpLayout();
        updateAnswer();
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));
    }

    @Override
    protected void setUpLayout() {
        super.setUpLayout();
        signButton = createSimpleButton(getContext(), questionDetails.isReadOnly(), getContext().getString(org.odk.collect.strings.R.string.sign_button), QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.LABEL_LARGE), this);

        answerLayout.addView(signButton);
        answerLayout.addView(errorTextView);
        answerLayout.addView(imageView);
    }

    @Override
    public Intent addExtrasToIntent(Intent intent) {
        return intent;
    }

    @Override
    protected boolean doesSupportDefaultValues() {
        return true;
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        // reset buttons
        signButton.setText(getContext().getString(org.odk.collect.strings.R.string.sign_button));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        signButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        signButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        imageClickHandler.clickImage("signButton");
    }
}
