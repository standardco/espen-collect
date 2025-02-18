package org.odk.collect.android.formentry;

import static org.odk.collect.settings.keys.ProjectKeys.KEY_BACKGROUND_RECORDING;

import android.Manifest;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.androidshared.livedata.LiveDataUtils;
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData;
import org.odk.collect.androidshared.livedata.NonNullLiveData;
import org.odk.collect.async.Cancellable;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.RecordingSession;
import org.odk.collect.permissions.PermissionsChecker;
import org.odk.collect.shared.settings.Settings;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class BackgroundAudioViewModel extends ViewModel {

    private final AudioRecorder audioRecorder;
    private final Settings generalSettings;
    private final RecordAudioActionRegistry recordAudioActionRegistry;
    private final PermissionsChecker permissionsChecker;
    private final Supplier<Long> clock;

    private final MutableNonNullLiveData<Boolean> isPermissionRequired = new MutableNonNullLiveData<>(false);
    private final MutableNonNullLiveData<Boolean> isBackgroundRecordingEnabled;

    // These fields handle storing record action details while we're granting permissions
    private final HashSet<TreeReference> tempTreeReferences = new HashSet<>();
    private final Cancellable formSessionObserver;
    private String tempQuality;

    @Nullable
    private AuditEventLogger auditEventLogger;

    public BackgroundAudioViewModel(AudioRecorder audioRecorder, Settings generalSettings, RecordAudioActionRegistry recordAudioActionRegistry, PermissionsChecker permissionsChecker, Supplier<Long> clock, LiveData<FormSession> formSession) {
        this.audioRecorder = audioRecorder;
        this.generalSettings = generalSettings;
        this.recordAudioActionRegistry = recordAudioActionRegistry;
        this.permissionsChecker = permissionsChecker;
        this.clock = clock;

        this.recordAudioActionRegistry.register((treeReference, quality) -> {
            new Handler(Looper.getMainLooper()).post(() -> handleRecordAction(treeReference, quality));
        });

        isBackgroundRecordingEnabled = new MutableNonNullLiveData<>(generalSettings.getBoolean(KEY_BACKGROUND_RECORDING));

        formSessionObserver = LiveDataUtils.observe(formSession, it -> this.auditEventLogger = it.getFormController().getAuditEventLogger());
    }

    @Override
    protected void onCleared() {
        recordAudioActionRegistry.unregister();
        formSessionObserver.cancel();
    }

    public LiveData<Boolean> isPermissionRequired() {
        return isPermissionRequired;
    }

    public NonNullLiveData<Boolean> isBackgroundRecordingEnabled() {
        return isBackgroundRecordingEnabled;
    }

    public void setBackgroundRecordingEnabled(boolean enabled) {
        if (enabled) {
            if (auditEventLogger != null) {
                auditEventLogger.logEvent(AuditEvent.AuditEventType.BACKGROUND_AUDIO_ENABLED, true, clock.get());
            }
        } else {
            audioRecorder.cleanUp();

            if (auditEventLogger != null) {
                auditEventLogger.logEvent(AuditEvent.AuditEventType.BACKGROUND_AUDIO_DISABLED, true, clock.get());
            }
        }

        generalSettings.save(KEY_BACKGROUND_RECORDING, enabled);
        isBackgroundRecordingEnabled.postValue(enabled);
    }

    public boolean isBackgroundRecording() {
        return audioRecorder.isRecording() && audioRecorder.getCurrentSession().getValue().getId() instanceof Set;
    }

    public void grantAudioPermission() {
        if (tempTreeReferences.isEmpty()) {
            throw new IllegalStateException("No TreeReferences to start recording with!");
        }

        isPermissionRequired.setValue(false);
        startBackgroundRecording(tempQuality, new HashSet<>(tempTreeReferences));

        tempTreeReferences.clear();
        tempQuality = null;
    }

    private void handleRecordAction(TreeReference treeReference, String quality) {
        if (isBackgroundRecordingEnabled.getValue()) {
            if (permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                if (isBackgroundRecording()) {
                    RecordingSession session = audioRecorder.getCurrentSession().getValue();
                    HashSet<TreeReference> treeReferences = (HashSet<TreeReference>) session.getId();
                    treeReferences.add(treeReference);
                } else {
                    HashSet<TreeReference> treeReferences = new HashSet<>();
                    treeReferences.add(treeReference);

                    startBackgroundRecording(quality, treeReferences);
                }
            } else {
                isPermissionRequired.setValue(true);

                tempTreeReferences.add(treeReference);
                if (tempQuality == null) {
                    tempQuality = quality;
                }
            }
        }
    }

    private void startBackgroundRecording(String quality, HashSet<TreeReference> treeReferences) {
        Output output = Output.AMR;
        if ("low".equals(quality)) {
            output = Output.AAC_LOW;
        } else if ("normal".equals(quality)) {
            output = Output.AAC;
        }

        audioRecorder.start(treeReferences, output);
    }

    public interface RecordAudioActionRegistry {

        void register(BiConsumer<TreeReference, String> listener);

        void unregister();
    }
}
