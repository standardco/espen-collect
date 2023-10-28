/*
 * Copyright (C) 2009 University of Washington
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

package org.espen.collect.android.activities;

import static org.espen.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.espen.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.espen.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.espen.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.espen.collect.android.analytics.AnalyticsEvents;
import org.espen.collect.android.analytics.AnalyticsUtils;
import org.espen.collect.android.dao.CursorLoaderFactory;
import org.espen.collect.android.database.instances.DatabaseInstanceColumns;
import org.espen.collect.android.external.FormUriActivity;
import org.espen.collect.android.external.InstancesContract;
import org.espen.collect.android.projects.ProjectsDataService;
import org.espen.collect.android.R;
import org.espen.collect.android.adapters.InstanceListCursorAdapter;
import org.espen.collect.android.analytics.AnalyticsEvents;
import org.espen.collect.android.analytics.AnalyticsUtils;
import org.espen.collect.android.dao.CursorLoaderFactory;
import org.espen.collect.android.database.instances.DatabaseInstanceColumns;
import org.espen.collect.android.external.FormUriActivity;
import org.espen.collect.android.external.InstancesContract;
import org.espen.collect.android.formlists.sorting.FormListSortingOption;
import org.espen.collect.android.injection.DaggerUtils;
import org.espen.collect.android.projects.ProjectsDataService;
import org.espen.collect.android.utilities.ApplicationConstants;
import org.espen.collect.android.utilities.FormsRepositoryProvider;
import org.espen.collect.androidshared.ui.multiclicksafe.MultiClickGuard;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;

import java.util.Arrays;

import javax.inject.Inject;

/**
 * Responsible for displaying all the valid instances in the instance directory.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceChooserList extends AppListActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String INSTANCE_LIST_ACTIVITY_SORTING_ORDER = "instanceListActivitySortingOrder";
    private static final String VIEW_SENT_FORM_SORTING_ORDER = "ViewSentFormSortingOrder";

    private static final boolean DO_NOT_EXIT = false;

    private boolean editMode;

    @Inject
    ProjectsDataService projectsDataService;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        setResult(RESULT_OK, result.getData());
        finish();
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_chooser_list);
        DaggerUtils.getComponent(this).inject(this);

        String formMode = getIntent().getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
        if (formMode == null || ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {
            setTitle(getString(org.odk.collect.strings.R.string.review_data));
            editMode = true;
        } else {
            setTitle(getString(org.odk.collect.strings.R.string.view_sent_forms));
            ((TextView) findViewById(android.R.id.empty)).setText(org.odk.collect.strings.R.string.no_items_display_sent_forms);
        }

        sortingOptions = Arrays.asList(
                new FormListSortingOption(
                        R.drawable.ic_sort_by_alpha,
                        org.odk.collect.strings.R.string.sort_by_name_asc
                ),
                new FormListSortingOption(
                        R.drawable.ic_sort_by_alpha,
                        org.odk.collect.strings.R.string.sort_by_name_desc
                ),
                new FormListSortingOption(
                        R.drawable.ic_access_time,
                        org.odk.collect.strings.R.string.sort_by_date_desc
                ),
                new FormListSortingOption(
                        R.drawable.ic_access_time,
                        org.odk.collect.strings.R.string.sort_by_date_asc
                )
        );

        init();
    }

    private void init() {
        setupAdapter();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            if (view.isEnabled()) {
                Cursor c = (Cursor) listView.getAdapter().getItem(position);
                long instanceId = c.getLong(c.getColumnIndex(DatabaseInstanceColumns._ID));
                Uri instanceUri = InstancesContract.getUri(projectsDataService.getCurrentProject().getUuid(), instanceId);

                String action = getIntent().getAction();
                if (Intent.ACTION_PICK.equals(action)) {
                    // caller is waiting on a picked form
                    setResult(RESULT_OK, new Intent().setData(instanceUri));
                    finish();
                } else {
                    // the form can be edited if it is incomplete or if, when it was
                    // marked as complete, it was determined that it could be edited
                    // later.
                    String status = c.getString(c.getColumnIndex(DatabaseInstanceColumns.STATUS));
                    String strCanEditWhenComplete =
                            c.getString(c.getColumnIndex(DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE));

                    boolean canEdit = status.equals(Instance.STATUS_INCOMPLETE)
                            || Boolean.parseBoolean(strCanEditWhenComplete);
                    if (!canEdit) {
                        createErrorDialog(getString(org.odk.collect.strings.R.string.cannot_edit_completed_form),
                                DO_NOT_EXIT);
                        return;
                    }
                    // caller wants to view/edit a form, so launch FormFillingActivity
                    Intent parentIntent = this.getIntent();
                    Intent intent = new Intent(this, FormUriActivity.class);
                    intent.setAction(Intent.ACTION_EDIT);
                    intent.setData(instanceUri);
                    String formMode = parentIntent.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
                    if (formMode == null || ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {
                        logFormEdit(c);
                        intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                        formLauncher.launch(intent);
                    } else {
                        intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.VIEW_SENT);
                        startActivity(intent);
                        finish();
                    }
                }
            } else {
                TextView disabledCause = view.findViewById(R.id.form_subtitle2);
                Toast.makeText(this, disabledCause.getText(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logFormEdit(Cursor cursor) {
        String status = cursor.getString(cursor.getColumnIndex(DatabaseInstanceColumns.STATUS));
        String formId = cursor.getString(cursor.getColumnIndex(DatabaseInstanceColumns.JR_FORM_ID));
        String version = cursor.getString(cursor.getColumnIndex(DatabaseInstanceColumns.JR_VERSION));

        Form form = formsRepositoryProvider.get().getLatestByFormIdAndVersion(formId, version);
        String formTitle = form != null ? form.getDisplayName() : "";

        if (status.equals(Instance.STATUS_INCOMPLETE)) {
            AnalyticsUtils.logFormEvent(AnalyticsEvents.EDIT_NON_FINALIZED_FORM, formId, formTitle);
        } else if (status.equals(Instance.STATUS_COMPLETE)) {
            AnalyticsUtils.logFormEvent(AnalyticsEvents.EDIT_FINALIZED_FORM, formId, formTitle);
        }
    }

    private void setupAdapter() {
        String[] data = {DatabaseInstanceColumns.DISPLAY_NAME, DatabaseInstanceColumns.DELETED_DATE};
        int[] view = {R.id.form_title, R.id.form_subtitle2};

        boolean shouldCheckDisabled = !editMode;
        listAdapter = new InstanceListCursorAdapter(
                this, R.layout.form_chooser_list_item, null, data, view, shouldCheckDisabled);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected String getSortingOrderKey() {
        return editMode ? INSTANCE_LIST_ACTIVITY_SORTING_ORDER : VIEW_SENT_FORM_SORTING_ORDER;
    }

    @Override
    protected void updateAdapter() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressBar();
        if (editMode) {
            return new CursorLoaderFactory(projectsDataService).createEditableInstancesCursorLoader(getFilterText(), getSortingOrder());
        } else {
            return new CursorLoaderFactory(projectsDataService).createSentInstancesCursorLoader(getFilterText(), getSortingOrder());
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        hideProgressBarAndAllow();
        listAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        listAdapter.swapCursor(null);
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this).create();
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(org.odk.collect.strings.R.string.ok), errorListener);
        alertDialog.show();
    }

    protected String getSortingOrder() {
        String sortingOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + DatabaseInstanceColumns.STATUS + " DESC";
        switch (getSelectedSortingOrder()) {
            case BY_NAME_ASC:
                sortingOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + DatabaseInstanceColumns.STATUS + " DESC";
                break;
            case BY_NAME_DESC:
                sortingOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + DatabaseInstanceColumns.STATUS + " DESC";
                break;
            case BY_DATE_ASC:
                sortingOrder = DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC";
                break;
            case BY_DATE_DESC:
                sortingOrder = DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
                break;
        }
        return sortingOrder;
    }
}
