package org.espen.collect.android.formentry.saving;

import android.net.Uri;

import org.espen.collect.android.javarosawrapper.FormController;
import org.espen.collect.android.tasks.SaveFormToDisk;
import org.espen.collect.android.tasks.SaveToDiskResult;
import org.espen.collect.android.utilities.MediaUtils;
import org.odk.collect.entities.storage.EntitiesRepository;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.lookup.LookUpRepository;
import java.util.ArrayList;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(Uri instanceContentURI, FormController formController, MediaUtils mediaUtils, boolean shouldFinalize, boolean exitAfter,
                                 String updatedSaveName, ProgressListener progressListener, ArrayList<String> tempFiles, String currentProjectId, EntitiesRepository entitiesRepository, InstancesRepository instancesRepository, LookUpRepository lookupRepository) {
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(formController, mediaUtils, exitAfter, shouldFinalize,
                updatedSaveName, instanceContentURI, tempFiles, currentProjectId, entitiesRepository, instancesRepository, lookupRepository);
        return saveFormToDisk.saveForm(progressListener);
    }
}
