package org.espen.collect.android.instrumented.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.FormDef;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.espen.collect.android.injection.DaggerUtils;
import org.espen.collect.android.injection.config.AppDependencyComponent;
import org.espen.collect.android.storage.StoragePathProvider;
import org.espen.collect.android.storage.StorageSubdirectory;
import org.espen.collect.android.support.StorageUtils;
import org.espen.collect.android.support.rules.RunnableRule;
import org.espen.collect.android.support.rules.TestRuleChain;
import org.espen.collect.android.tasks.FormLoaderTask;
import org.espen.collect.android.tasks.FormLoaderTask.FormEntryControllerFactory;
import org.odk.collect.projects.Project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class FormLoaderTaskTest {

    private final StoragePathProvider storagePathProvider = new StoragePathProvider();

    private static final String SECONDARY_INSTANCE_EXTERNAL_CSV_FORM = "external_csv_form.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FORM = "simple-search-external-csv.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FILE = "simple-search-external-csv-fruits.csv";
    private static final String SIMPLE_SEARCH_EXTERNAL_DB_FILE = "simple-search-external-csv-fruits.db";

    private final FormEntryControllerFactory formEntryControllerFactory = new FormEntryControllerFactory() {
        @Override
        public FormEntryController create(FormDef formDef) {
            return new FormEntryController(new FormEntryModel(formDef));
        }
    };

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(new RunnableRule(() -> {
                try {
                    // Set up demo project
                    AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext());
                    component.projectsRepository().save(Project.Companion.getDEMO_PROJECT());
                    component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID);

                    StorageUtils.copyFormToDemoProject(SECONDARY_INSTANCE_EXTERNAL_CSV_FORM, Arrays.asList("external_csv_cities.csv", "external_csv_countries.csv", "external_csv_neighbourhoods.csv"));
                    StorageUtils.copyFormToDemoProject(SIMPLE_SEARCH_EXTERNAL_CSV_FORM, Collections.singletonList(SIMPLE_SEARCH_EXTERNAL_CSV_FILE));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

    // Validate the use of CSV files as secondary instances accessed through "jr://file-csv"
    @Test
    public void loadFormWithSecondaryCSV() throws Exception {
        final String formPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + File.separator + SECONDARY_INSTANCE_EXTERNAL_CSV_FORM;
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null, formEntryControllerFactory, mock());
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.executeSynchronously(formPath);
        Assert.assertNotNull(wrapper);
    }

    // Validate the use of a CSV file externally accessed through search/pulldata
    @Test
    public void loadSearchFromExternalCSV() throws Exception {
        final String formPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM;
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null, formEntryControllerFactory, mock());
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.executeSynchronously(formPath);
        assertThat(wrapper, notNullValue());
    }

    @Test
    public void loadSearchFromexternalCsvLeavesFileUnchanged() throws Exception {
        final String formPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM;
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null, formEntryControllerFactory, mock());
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.executeSynchronously(formPath);
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());

        File mediaFolder = wrapper.getController().getMediaFolder();
        File importedCSV = new File(mediaFolder + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FILE);
        Assert.assertTrue("Expected the imported CSV file to remain unchanged", importedCSV.exists());
    }

    // Validate that importing external data multiple times does not fail due to side effects from import
    @Test
    public void loadSearchFromExternalCSVmultipleTimes() throws Exception {
        final String formPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM;
        // initial load with side effects
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null, formEntryControllerFactory, mock());
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.executeSynchronously(formPath);
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());

        File mediaFolder = wrapper.getController().getMediaFolder();
        File dbFile = new File(mediaFolder + File.separator + SIMPLE_SEARCH_EXTERNAL_DB_FILE);
        Assert.assertTrue(dbFile.exists());
        long dbLastModified = dbFile.lastModified();

        // subsequent load should succeed despite side effects from import
        formLoaderTask = new FormLoaderTask(formPath, null, null, formEntryControllerFactory, mock());
        wrapper = formLoaderTask.executeSynchronously(formPath);
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());
        Assert.assertEquals("expected file modification timestamp to be unchanged", dbLastModified, dbFile.lastModified());
    }
}
