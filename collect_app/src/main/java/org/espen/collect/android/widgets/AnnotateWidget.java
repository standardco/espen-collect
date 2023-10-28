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

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static org.espen.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;
import static org.espen.collect.android.utilities.ApplicationConstants.RequestCodes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import org.espen.collect.android.draw.DrawActivity;
import org.espen.collect.android.utilities.Appearances;
import org.espen.collect.android.utilities.ApplicationConstants;
import org.espen.collect.android.utilities.ContentUriProvider;
import org.espen.collect.android.utilities.FileUtils;
import org.espen.collect.android.utilities.QuestionMediaManager;
import org.espen.collect.android.widgets.interfaces.ButtonClickListener;
import org.espen.collect.android.widgets.utilities.QuestionFontSizeUtils;
import org.espen.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.espen.collect.android.BuildConfig;
import org.espen.collect.android.R;
import org.espen.collect.android.draw.DrawActivity;
import org.espen.collect.android.formentry.questions.QuestionDetails;
import org.espen.collect.android.formentry.questions.WidgetViewUtils;
import org.espen.collect.android.utilities.Appearances;
import org.espen.collect.android.utilities.ContentUriProvider;
import org.espen.collect.android.utilities.FileUtils;
import org.espen.collect.android.utilities.QuestionMediaManager;
import org.espen.collect.android.widgets.interfaces.ButtonClickListener;
import org.espen.collect.android.widgets.utilities.QuestionFontSizeUtils;
import org.espen.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.espen.collect.androidshared.ui.ToastUtils;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

/**
 * Image widget that supports annotations on the image.
 *
 * @author BehrAtherton@gmail.com
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class AnnotateWidget extends BaseImageWidget implements ButtonClickListener {

    Button captureButton;
    Button chooseButton;
    Button annotateButton;

    public AnnotateWidget(Context context, QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry, String tmpImageFilePath) {
        super(context, prompt, questionMediaManager, waitingForDataRegistry, tmpImageFilePath);
        render();

        imageClickHandler = new DrawImageClickHandler(DrawActivity.OPTION_ANNOTATE, ApplicationConstants.RequestCodes.ANNOTATE_IMAGE, org.odk.collect.strings.R.string.annotate_image);
        imageCaptureHandler = new ImageCaptureHandler();
        setUpLayout();
        updateAnswer();
        adjustAnnotateButtonAvailability();
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));
    }

    @Override
    protected void setUpLayout() {
        super.setUpLayout();
        captureButton = createSimpleButton(getContext(), R.id.capture_image, questionDetails.isReadOnly(), getContext().getString(org.odk.collect.strings.R.string.capture_image), QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.LABEL_LARGE), this);

        chooseButton = createSimpleButton(getContext(), R.id.choose_image, questionDetails.isReadOnly(), getContext().getString(org.odk.collect.strings.R.string.choose_image), QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.LABEL_LARGE), this);

        annotateButton = createSimpleButton(getContext(), R.id.markup_image, questionDetails.isReadOnly(), getContext().getString(org.odk.collect.strings.R.string.markup_image), QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.LABEL_LARGE), this);

        annotateButton.setOnClickListener(v -> imageClickHandler.clickImage("annotateButton"));

        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(annotateButton);
        answerLayout.addView(errorTextView);
        answerLayout.addView(imageView);

        hideButtonsIfNeeded();
    }

    @Override
    public Intent addExtrasToIntent(Intent intent) {
        intent.putExtra(DrawActivity.SCREEN_ORIENTATION, calculateScreenOrientation());
        return intent;
    }

    @Override
    protected boolean doesSupportDefaultValues() {
        return true;
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        annotateButton.setEnabled(false);

        // reset buttons
        captureButton.setText(getContext().getString(org.odk.collect.strings.R.string.capture_image));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
        annotateButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
        annotateButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        if (buttonId == R.id.capture_image) {
            getPermissionsProvider().requestCameraPermission((Activity) getContext(), this::captureImage);
        } else if (buttonId == R.id.choose_image) {
            imageCaptureHandler.chooseImage(org.odk.collect.strings.R.string.annotate_image);
        }
    }

    private void adjustAnnotateButtonAvailability() {
        if (binaryName == null || imageView.getVisibility() == GONE) {
            annotateButton.setEnabled(false);
        }
    }

    private void hideButtonsIfNeeded() {
        if (getFormEntryPrompt().getAppearanceHint() != null
                && getFormEntryPrompt().getAppearanceHint().toLowerCase(Locale.ENGLISH).contains(Appearances.NEW)) {
            chooseButton.setVisibility(GONE);
        }
    }

    private int calculateScreenOrientation() {
        Bitmap bmp = null;
        if (imageView.getDrawable() != null) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        }

        return bmp != null && bmp.getHeight() > bmp.getWidth() ?
                SCREEN_ORIENTATION_PORTRAIT : SCREEN_ORIENTATION_LANDSCAPE;
    }

    private void captureImage() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        // We give the camera an absolute filename/path where to put the
        // picture because of bug:
        // http://code.google.com/p/android/issues/detail?id=1480
        // The bug appears to be fixed in Android 2.0+, but as of feb 2,
        // 2010, G1 phones only run 1.6. Without specifying the path the
        // images returned by the camera in 1.6 (and earlier) are ~1/4
        // the size. boo.

        try {
            Uri uri = new ContentUriProvider().getUriForFile(getContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    new File(tmpImageFilePath));
            // if this gets modified, the onActivityResult in
            // FormEntyActivity will also need to be updated.
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            FileUtils.grantFilePermissions(intent, uri, getContext());
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }

        imageCaptureHandler.captureImage(intent, ApplicationConstants.RequestCodes.IMAGE_CAPTURE, org.odk.collect.strings.R.string.annotate_image);
    }

    @Override
    public void setData(Object newImageObj) {
        if (newImageObj instanceof File) {
            String mimeType = FileUtils.getMimeType((File) newImageObj);
            if ("image/gif".equals(mimeType)) {
                ToastUtils.showLongToast(getContext(), org.odk.collect.strings.R.string.gif_not_supported);
            } else {
                super.setData(newImageObj);
                annotateButton.setEnabled(binaryName != null);
            }
        }
    }
}
