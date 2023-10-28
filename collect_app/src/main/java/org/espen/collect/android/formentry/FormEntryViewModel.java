package org.espen.collect.android.formentry;

import static org.espen.collect.android.javarosawrapper.FormIndexUtils.getRepeatGroupIndex;
import static org.espen.collect.androidshared.livedata.LiveDataUtils.observe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.espen.collect.android.exception.ExternalDataException;
import org.espen.collect.android.exception.JavaRosaException;
import org.espen.collect.android.formentry.audit.AuditEvent;
import org.espen.collect.android.formentry.questions.SelectChoiceUtils;
import org.espen.collect.android.javarosawrapper.FailedValidationResult;
import org.espen.collect.android.javarosawrapper.FormController;
import org.espen.collect.android.javarosawrapper.FormIndexUtils;
import org.espen.collect.android.javarosawrapper.ValidationResult;
import org.espen.collect.android.widgets.interfaces.SelectChoiceLoader;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.actions.recordaudio.RecordAudioActionHandler;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.espen.collect.android.exception.ExternalDataException;
import org.espen.collect.android.exception.JavaRosaException;
import org.espen.collect.android.formentry.audit.AuditEvent;
import org.espen.collect.android.formentry.questions.SelectChoiceUtils;
import org.espen.collect.android.javarosawrapper.FailedValidationResult;
import org.espen.collect.android.javarosawrapper.FormController;
import org.espen.collect.android.javarosawrapper.ValidationResult;
import org.espen.collect.android.widgets.interfaces.SelectChoiceLoader;
import org.espen.collect.androidshared.data.Consumable;
import org.espen.collect.androidshared.livedata.MutableNonNullLiveData;
import org.espen.collect.androidshared.livedata.NonNullLiveData;
import org.odk.collect.async.Cancellable;
import org.odk.collect.async.Scheduler;
import org.odk.collect.lookup.LookUpRepository;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class FormEntryViewModel extends ViewModel implements SelectChoiceLoader {

    private final Supplier<Long> clock;
    private final Scheduler scheduler;

    private final MutableLiveData<FormError> error = new MutableLiveData<>(null);
    private final MutableNonNullLiveData<Boolean> hasBackgroundRecording = new MutableNonNullLiveData<>(false);
    private final MutableLiveData<FormIndex> currentIndex = new MutableLiveData<>(null);
    private final MutableNonNullLiveData<Boolean> isLoading = new MutableNonNullLiveData<>(false);
    private final MutableLiveData<Consumable<ValidationResult>>
            validationResult = new MutableLiveData<>(new Consumable<>(null));
    @NonNull
    private final FormSessionRepository formSessionRepository;
    @NonNull
    private final LookUpRepository lookUpRepository;
    private final String sessionId;

    @Nullable
    private FormController formController;

    @Nullable
    private FormIndex jumpBackIndex;

    @Nullable
    private AnswerListener answerListener;

    private final Cancellable formSessionObserver;

    @SuppressWarnings("WeakerAccess")
    public FormEntryViewModel(Supplier<Long> clock, Scheduler scheduler, FormSessionRepository formSessionRepository,LookUpRepository lookUpRepository, String sessionId) {
        this.clock = clock;
        this.scheduler = scheduler;
        this.formSessionRepository = formSessionRepository;
        this.lookUpRepository = lookUpRepository;
        this.sessionId = sessionId;
        formSessionObserver = observe(formSessionRepository.get(this.sessionId), formSession -> {
            this.formController = formSession.getFormController();

            boolean hasBackgroundRecording = formController.getFormDef().hasAction(RecordAudioActionHandler.ELEMENT_NAME);
            this.hasBackgroundRecording.setValue(hasBackgroundRecording);
        });
    }

    public String getSessionId() {
        return sessionId;
    }

    /**
     * @deprecated this should not be exposed
     */
    @Deprecated
    public FormController getFormController() {
        return formController;
    }

    public LiveData<FormIndex> getCurrentIndex() {
        return currentIndex;
    }

    public LiveData<FormError> getError() {
        return error;
    }

    public LiveData<Consumable<ValidationResult>> getValidationResult() {
        return validationResult;
    }

    public NonNullLiveData<Boolean> isLoading() {
        return isLoading;
    }

    @SuppressWarnings("WeakerAccess")
    public void promptForNewRepeat() {
        if (formController == null) {
            return;
        }

        jumpBackIndex = formController.getFormIndex();
        jumpToNewRepeat();
        refresh();
    }

    public void jumpToNewRepeat() {
        formController.jumpToNewRepeatPrompt();
    }

    public void addRepeat() {
        if (formController == null) {
            return;
        }

        jumpBackIndex = null;

        try {
            formController.newRepeat();
        } catch (RuntimeException e) {
            error.setValue(new FormError.NonFatal(e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        if (!formController.indexIsInFieldList()) {
            try {
                formController.stepToNextScreenEvent();
            } catch (JavaRosaException e) {
                error.setValue(new FormError.NonFatal(e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            }
        }

        refresh();
    }

    public void cancelRepeatPrompt() {
        if (formController == null) {
            return;
        }

        if (jumpBackIndex != null) {
            formController.jumpToIndex(jumpBackIndex);
            jumpBackIndex = null;
        } else {
            try {
                this.formController.stepToNextScreenEvent();
            } catch (JavaRosaException exception) {
                error.setValue(new FormError.NonFatal(exception.getCause().getMessage()));
            }
        }

        refresh();
    }

    public void errorDisplayed() {
        error.setValue(null);
    }

    public boolean canAddRepeat() {
        if (formController != null && formController.indexContainsRepeatableGroup()) {
            FormDef formDef = formController.getFormDef();
            FormIndex repeatGroupIndex = FormIndexUtils.getRepeatGroupIndex(formController.getFormIndex(), formDef);
            return !((GroupDef) formDef.getChild(repeatGroupIndex)).noAddRemove;
        } else {
            return false;
        }
    }

    public void moveForward(HashMap<FormIndex, IAnswerData> answers) {
        moveForward(answers, false);
    }

    public void moveForward(HashMap<FormIndex, IAnswerData> answers, Boolean evaluateConstraints) {
        isLoading.setValue(true);

        scheduler.immediate((Supplier<Boolean>) () -> {
            return saveScreenAnswersToFormController(answers, evaluateConstraints);
        }, updateSuccess -> {
            isLoading.setValue(false);

            if (updateSuccess) {
                try {
                    formController.stepToNextScreenEvent();
                } catch (JavaRosaException e) {
                    error.setValue(new FormError.NonFatal(e.getCause().getMessage()));
                }

                formController.getAuditEventLogger().flush(); // Close events waiting for an end time
                refresh();
            }
        });
    }

    public void moveBackward(HashMap<FormIndex, IAnswerData> answers) {
        isLoading.setValue(true);

        scheduler.immediate((Supplier<Boolean>) () -> {
            return saveScreenAnswersToFormController(answers, false);
        }, updateSuccess -> {
            isLoading.setValue(false);

            if (updateSuccess) {
                try {
                    formController.stepToPreviousScreenEvent();
                } catch (JavaRosaException e) {
                    error.setValue(new FormError.NonFatal(e.getCause().getMessage()));
                    return;
                }

                formController.getAuditEventLogger().flush(); // Close events waiting for an end time
                refresh();
            }
        });
    }

    public boolean updateAnswersForScreen(HashMap<FormIndex, IAnswerData> answers, Boolean evaluateConstraints) {
        boolean success = saveScreenAnswersToFormController(answers, evaluateConstraints);
        formController.getAuditEventLogger().flush();

        return success;
    }

    public boolean saveScreenAnswersToFormController(HashMap<FormIndex, IAnswerData> answers, Boolean evaluateConstraints) {
        if (formController == null) {
            return false;
        }

        try {
            ValidationResult result = formController.saveAllScreenAnswers(answers, evaluateConstraints);
            if (result instanceof FailedValidationResult) {
                validationResult.postValue(new Consumable<>(result));
                return false;
            }
        } catch (JavaRosaException e) {
            error.postValue(new FormError.NonFatal(e.getMessage()));
            return false;
        }

        return true;
    }

    public void openHierarchy() {
        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.HIERARCHY, true, clock.get());
    }

    public NonNullLiveData<Boolean> hasBackgroundRecording() {
        return hasBackgroundRecording;
    }

    public FormEntryPrompt getQuestionPrompt(FormIndex formIndex) {
        return formController.getQuestionPrompt(formIndex);
    }

    public void setAnswerListener(@Nullable AnswerListener answerListener) {
        this.answerListener = answerListener;
    }

    public void answerQuestion(FormIndex index, IAnswerData answer) {
        if (this.answerListener != null) {
            this.answerListener.onAnswer(index, answer);
        }
    }

    @NonNull
    @Override
    public List<SelectChoice> loadSelectChoices(@NonNull FormEntryPrompt prompt) throws FileNotFoundException, XPathSyntaxException, ExternalDataException {
        return SelectChoiceUtils.loadSelectChoices(prompt, formController, lookUpRepository);
    }

    @Override
    protected void onCleared() {
        this.answerListener = null;
        formSessionObserver.cancel();
    }

    public void refresh() {
        if (formController != null) {
            currentIndex.setValue(formController.getFormIndex());
        }
    }

    public void exit() {
        formSessionRepository.clear(sessionId);
    }

    public void validate() {
        isLoading.setValue(true);
        scheduler.immediate(
                () -> {
                    ValidationResult result = null;
                    try {
                        result = formController.validateAnswers(true);
                    } catch (JavaRosaException e) {
                        error.postValue(new FormError.NonFatal(e.getMessage()));
                    }

                    return result;
                }, result -> {
                    isLoading.setValue(false);

                    if (result instanceof FailedValidationResult) {
                        refresh();
                    }
                    validationResult.setValue(new Consumable<>(result));
                }
        );
    }

    public interface AnswerListener {
        void onAnswer(FormIndex index, IAnswerData answer);
    }
}
