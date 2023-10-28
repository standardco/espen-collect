package org.espen.collect.android.widgets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.espen.collect.android.widgets.VideoWidget;
import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.espen.collect.android.R;
import org.espen.collect.android.formentry.questions.QuestionDetails;
import org.espen.collect.android.injection.config.AppDependencyModule;
import org.espen.collect.android.support.CollectHelpers;
import org.espen.collect.android.utilities.MediaUtils;
import org.espen.collect.android.widgets.base.FileWidgetTest;
import org.espen.collect.android.widgets.support.FakeQuestionMediaManager;
import org.espen.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.espen.collect.androidshared.system.IntentLauncher;
import org.odk.collect.shared.TempFiles;

/**
 * @author James Knight
 */
public class VideoWidgetTest extends FileWidgetTest<VideoWidget> {
    private String destinationName;

    @NonNull
    @Override
    public VideoWidget createWidget() {
        return new VideoWidget(activity, new QuestionDetails(formEntryPrompt, readOnlyOverride), new FakeWaitingForDataRegistry(), new FakeQuestionMediaManager());
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(destinationName);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        destinationName = RandomString.make();
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        MediaUtils mediaUtils = mock(MediaUtils.class);
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public MediaUtils providesMediaUtils(IntentLauncher intentLauncher) {
                return mediaUtils;
            }
        });

        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.capture_video);
        assertActionEquals(MediaStore.ACTION_VIDEO_CAPTURE, intent);

        intent = getIntentLaunchedByClick(R.id.choose_video);
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent);
        assertTypeEquals("video/*", intent);

        getWidget().setData(TempFiles.createTempFile(TempFiles.createTempDir()));
        getIntentLaunchedByClick(R.id.play_video);
        verify(mediaUtils).openFile(any(), any(), any());
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertNull(getIntentLaunchedByClick(R.id.capture_video));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().captureButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().chooseButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().playButton.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().playButton.isEnabled(), is(Boolean.FALSE));
        assertThat(getSpyWidget().playButton.getText(), is("Play Video"));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.isReadOnly()).thenReturn(false);

        assertThat(getSpyWidget().captureButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().chooseButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().playButton.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().playButton.isEnabled(), is(Boolean.FALSE));
        assertThat(getSpyWidget().playButton.getText(), is("Play Video"));
    }
}
