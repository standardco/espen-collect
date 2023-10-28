package org.espen.collect.android.widgets;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.espen.collect.android.widgets.ExStringWidget;
import org.javarosa.core.model.data.StringData;
import org.mockito.Mock;
import org.espen.collect.android.formentry.questions.QuestionDetails;
import org.espen.collect.android.widgets.base.GeneralExStringWidgetTest;
import org.espen.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.espen.collect.android.widgets.utilities.StringRequester;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class ExStringWidgetTest extends GeneralExStringWidgetTest<ExStringWidget, StringData> {

    @Mock
    StringRequester stringRequester;

    @NonNull
    @Override
    public ExStringWidget createWidget() {
        return new ExStringWidget(activity, new QuestionDetails(formEntryPrompt), new FakeWaitingForDataRegistry(), stringRequester);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
    }
}
