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
import static org.espen.collect.android.injection.DaggerUtils.getComponent;
import static org.espen.collect.android.utilities.ApplicationConstants.RequestCodes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.espen.collect.android.externaldata.ExternalAppsUtils;
import org.espen.collect.android.injection.DaggerUtils;
import org.espen.collect.android.utilities.ApplicationConstants;
import org.espen.collect.android.widgets.interfaces.ButtonClickListener;
import org.espen.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.espen.collect.android.widgets.utilities.QuestionFontSizeUtils;
import org.espen.collect.android.widgets.utilities.StringRequester;
import org.espen.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.javarosa.core.model.data.StringData;
import org.espen.collect.android.externaldata.ExternalAppsUtils;
import org.espen.collect.android.formentry.questions.QuestionDetails;
import org.espen.collect.android.formentry.questions.WidgetViewUtils;
import org.espen.collect.android.widgets.interfaces.ButtonClickListener;
import org.espen.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.espen.collect.android.widgets.utilities.QuestionFontSizeUtils;
import org.espen.collect.android.widgets.utilities.StringRequester;
import org.espen.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.Serializable;

import timber.log.Timber;

/**
 * <p>Launch an external app to supply a string value. If the app
 * does not launch, enable the text area for regular data entry.</p>
 * <p>
 * <p>The default button text is "Launch"
 * <p>
 * <p>You may override the button text and the error text that is
 * displayed when the app is missing by using jr:itext() values.
 * <p>
 * <p>To use this widget, define an appearance on the &lt;input/&gt;
 * tag that begins "ex:" and then contains the intent action to lauch.
 * <p>
 * <p>e.g.,
 * <p>
 * <pre>
 * &lt;input appearance="ex:change.uw.android.TEXTANSWER" ref="/form/passPhrase" &gt;
 * </pre>
 * <p>or, to customize the button text and error strings with itext:
 * <pre>
 *      ...
 *      &lt;bind nodeset="/form/passPhrase" type="string" /&gt;
 *      ...
 *      &lt;itext&gt;
 *        &lt;translation lang="English"&gt;
 *          &lt;text id="textAnswer"&gt;
 *            &lt;value form="short"&gt;Text question&lt;/value&gt;
 *            &lt;value form="long"&gt;Enter your pass phrase&lt;/value&gt;
 *            &lt;value form="buttonText"&gt;Get Pass Phrase&lt;/value&gt;
 *            &lt;value form="noAppErrorString"&gt;Pass Phrase Tool is not installed!
 *             Please proceed to manually enter pass phrase.&lt;/value&gt;
 *          &lt;/text&gt;
 *        &lt;/translation&gt;
 *      &lt;/itext&gt;
 *    ...
 *    &lt;input appearance="ex:change.uw.android.TEXTANSWER" ref="/form/passPhrase"&gt;
 *      &lt;label ref="jr:itext('textAnswer')"/&gt;
 *    &lt;/input&gt;
 * </pre>
 */
@SuppressLint("ViewConstructor")
public class ExStringWidget extends StringWidget implements WidgetDataReceiver, ButtonClickListener {
    private final WaitingForDataRegistry waitingForDataRegistry;

    private boolean hasExApp = true;
    public Button launchIntentButton;
    private final StringRequester stringRequester;

    public ExStringWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry, StringRequester stringRequester) {
        super(context, questionDetails);
        render();

        this.waitingForDataRegistry = waitingForDataRegistry;
        this.stringRequester = stringRequester;
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    protected void setUpLayout(Context context) {
        answerText.setText(getFormEntryPrompt().getAnswerText());
        launchIntentButton = createSimpleButton(getContext(), getFormEntryPrompt().isReadOnly(), getButtonText(), QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.LABEL_LARGE), this);

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(launchIntentButton);
        answerLayout.addView(answerText);
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));
    }

    private String getButtonText() {
        String v = getFormEntryPrompt().getSpecialFormQuestionText("buttonText");
        return v != null ? v : getContext().getString(org.odk.collect.strings.R.string.launch_app);
    }

    protected Serializable getAnswerForIntent() {
        return getFormEntryPrompt().getAnswerText();
    }

    protected int getRequestCode() {
        return ApplicationConstants.RequestCodes.EX_STRING_CAPTURE;
    }

    @Override
    public void setData(Object answer) {
        StringData stringData = ExternalAppsUtils.asStringData(answer);
        answerText.setText(stringData == null ? null : stringData.getValue().toString());
        widgetValueChanged();
    }

    @Override
    public void setFocus(Context context) {
        if (hasExApp) {
            softKeyboardController.hideSoftKeyboard(answerText);
            // focus on launch button
            launchIntentButton.requestFocus();
        } else {
            if (!getFormEntryPrompt().isReadOnly()) {
                softKeyboardController.showSoftKeyboard(answerText);
            /*
             * If you do a multi-question screen after a "add another group" dialog, this won't
             * automatically pop up. It's an Android issue.
             *
             * That is, if I have an edit text in an activity, and pop a dialog, and in that
             * dialog's button's OnClick() I call edittext.requestFocus() and
             * showSoftInput(edittext, 0), showSoftinput() returns false. However, if the
             * edittext
             * is focused before the dialog pops up, everything works fine. great.
             */
            } else {
                softKeyboardController.hideSoftKeyboard(answerText);
            }
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        answerText.setOnLongClickListener(l);
        launchIntentButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        answerText.cancelLongPress();
        launchIntentButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
        stringRequester.launch((Activity) getContext(), getRequestCode(), getFormEntryPrompt(), getAnswerForIntent(), (String errorMsg) -> {
            onException(errorMsg);
            return null;
        });
    }

    private void focusAnswer() {
        softKeyboardController.showSoftKeyboard(answerText);
    }

    private void onException(String toastText) {
        hasExApp = false;
        if (!getFormEntryPrompt().isReadOnly()) {
            answerText.setBackground((new EditText(getContext())).getBackground());
            answerText.setFocusable(true);
            answerText.setFocusableInTouchMode(true);
            answerText.setEnabled(true);
            answerText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    widgetValueChanged();
                }
            });
        }
        launchIntentButton.setEnabled(false);
        launchIntentButton.setFocusable(false);
        waitingForDataRegistry.cancelWaitingForData();

        Toast.makeText(getContext(),
                toastText, Toast.LENGTH_SHORT)
                .show();
        Timber.d(toastText);
        focusAnswer();
        Selection.setSelection(answerText.getText(), answerText.getText().toString().length());
    }
}
