package org.espen.collect.android.backgroundwork;

public interface InstanceSubmitScheduler {

    void scheduleSubmit(String projectId);

    void cancelSubmit(String projectId);
}
