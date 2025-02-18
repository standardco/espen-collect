package org.espen.collect.android.formentry.saving;

import android.net.Uri;

import org.espen.collect.android.javarosawrapper.FormController;
import org.espen.collect.android.tasks.SaveToDiskResult;
import org.espen.collect.android.utilities.MediaUtils;
import org.odk.collect.entities.storage.EntitiesRepository;
import org.odk.collect.forms.instances.InstancesRepository;

import java.util.ArrayList;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, MediaUtils mediaUtils, boolean shouldFinalize, boolean exitAfter,
                          String updatedSaveName, ProgressListener progressListener, ArrayList<String> tempFiles, String currentProjectId, EntitiesRepository entitiesRepository, InstancesRepository instancesRepository);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
