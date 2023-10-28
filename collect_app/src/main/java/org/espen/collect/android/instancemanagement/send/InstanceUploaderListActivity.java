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

package org.espen.collect.android.instancemanagement.send;

import static org.espen.collect.android.activities.AppListActivity.LOADER_ID;
import static org.espen.collect.android.activities.AppListActivity.toggleButtonLabel;
import static org.espen.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.espen.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.espen.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.espen.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;
import static org.espen.collect.androidshared.ui.MultiSelectViewModelKt.updateSelectAll;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_PROTOCOL;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.espen.collect.android.activities.AppListActivity;
import org.espen.collect.android.activities.FormFillingActivity;
import org.espen.collect.android.adapters.InstanceUploaderAdapter;
import org.espen.collect.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler;
import org.espen.collect.android.dao.CursorLoaderFactory;
import org.espen.collect.android.database.instances.DatabaseInstanceColumns;
import org.espen.collect.android.formlists.sorting.FormListSortingBottomSheetDialog;
import org.espen.collect.android.formlists.sorting.FormListSortingOption;
import org.espen.collect.android.injection.DaggerUtils;
import org.espen.collect.android.mainmenu.MainMenuActivity;
import org.espen.collect.android.preferences.screens.ProjectPreferencesActivity;
import org.espen.collect.android.projects.ProjectsDataService;
import org.espen.collect.android.utilities.ApplicationConstants;
import org.espen.collect.android.R;
import org.espen.collect.android.activities.FormFillingActivity;
import org.espen.collect.android.adapters.InstanceUploaderAdapter;
import org.espen.collect.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler;
import org.espen.collect.android.backgroundwork.InstanceSubmitScheduler;
import org.espen.collect.android.dao.CursorLoaderFactory;
import org.espen.collect.android.database.instances.DatabaseInstanceColumns;
import org.espen.collect.android.databinding.InstanceUploaderListBinding;
import org.espen.collect.android.formlists.sorting.FormListSortingBottomSheetDialog;
import org.espen.collect.android.formlists.sorting.FormListSortingOption;
import org.espen.collect.android.formmanagement.FormFillingIntentFactory;
import org.espen.collect.android.gdrive.GoogleSheetsUploaderActivity;
import org.espen.collect.android.injection.DaggerUtils;
import org.espen.collect.android.mainmenu.MainMenuActivity;
import org.espen.collect.android.preferences.screens.ProjectPreferencesActivity;
import org.espen.collect.android.projects.ProjectsDataService;
import org.espen.collect.androidshared.system.PlayServicesChecker;
import org.espen.collect.androidshared.network.NetworkStateProvider;
import org.espen.collect.androidshared.ui.MultiSelectViewModel;
import org.espen.collect.androidshared.ui.ToastUtils;
import org.espen.collect.androidshared.ui.multiclicksafe.MultiClickGuard;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.settings.keys.ProjectKeys;
import org.odk.collect.strings.localization.LocalizedActivity;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenuActivity}.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class InstanceUploaderListActivity extends LocalizedActivity implements
        OnLongClickListener, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String SHOW_ALL_MODE = "showAllMode";
    private static final String INSTANCE_UPLOADER_LIST_SORTING_ORDER = "instanceUploaderListSortingOrder";

    private static final String IS_SEARCH_BOX_SHOWN = "isSearchBoxShown";
    private static final String SEARCH_TEXT = "searchText";

    private static final int INSTANCE_UPLOADER = 0;

    InstanceUploaderListBinding binding;

    @Inject
    ProjectsDataService projectsDataService;

    private boolean showAllMode;

    // Default to true so the send button is disabled until the worker status is updated by the
    // observer
    private boolean autoSendOngoing = true;

    @Inject
    NetworkStateProvider connectivityProvider;

    @Inject
    InstanceSubmitScheduler instanceSubmitScheduler;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    ReadyToSendViewModel.Factory factory;

    private ListView listView;
    private InstanceUploaderAdapter listAdapter;
    private Integer selectedSortingOrder;
    private List<FormListSortingOption> sortingOptions;
    private ProgressBar progressBar;
    private String filterText;

    private MultiSelectViewModel multiSelectViewModel;
    private ReadyToSendViewModel readyToSendViewModel;
    private boolean allSelected;

    private boolean isSearchBoxShown;

    private SearchView searchView;
    private String savedFilterText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate");

        if (savedInstanceState != null) {
            isSearchBoxShown = savedInstanceState.getBoolean(IS_SEARCH_BOX_SHOWN);
            savedFilterText = savedInstanceState.getString(SEARCH_TEXT);
        }

        DaggerUtils.getComponent(this).inject(this);

        multiSelectViewModel = new ViewModelProvider(this).get(MultiSelectViewModel.class);
        multiSelectViewModel.getSelected().observe(this, ids -> {
            binding.uploadButton.setEnabled(!ids.isEmpty());
            allSelected = updateSelectAll(binding.toggleButton, listAdapter.getCount(), ids.size());

            listAdapter.setSelected(ids);
        });
        readyToSendViewModel = new ViewModelProvider(this, factory).get(ReadyToSendViewModel.class);
        readyToSendViewModel.getData().observe(this, data -> binding.readyToSendBanner.setData(data));

        // set title
        setTitle(getString(org.odk.collect.strings.R.string.send_data));
        binding = InstanceUploaderListBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.uploadButton.setOnClickListener(v -> onUploadButtonsClicked());
        if (savedInstanceState != null) {
            showAllMode = savedInstanceState.getBoolean(SHOW_ALL_MODE);
        }
    }

    public void onUploadButtonsClicked() {
        if (!connectivityProvider.isDeviceOnline()) {
            ToastUtils.showShortToast(this, org.odk.collect.strings.R.string.no_connection);
            return;
        }

        if (autoSendOngoing) {
            ToastUtils.showShortToast(this, org.odk.collect.strings.R.string.send_in_progress);
            return;
        }

        Set<Long> selectedItems = multiSelectViewModel.getSelected().getValue();
        if (!selectedItems.isEmpty()) {
            binding.uploadButton.setEnabled(false);

            uploadSelectedFiles(selectedItems.stream().mapToLong(Long::longValue).toArray());
            multiSelectViewModel.unselectAll();
        } else {
            // no items selected
            ToastUtils.showLongToast(this, org.odk.collect.strings.R.string.noselect_error);
        }
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        listView = findViewById(android.R.id.list);
        listView.setOnItemClickListener((AdapterView.OnItemClickListener) this);
        listView.setEmptyView(findViewById(android.R.id.empty));
        progressBar = findViewById(R.id.progressBar);

        // Use the nicer-looking drawable with Material Design insets.
        listView.setDivider(ContextCompat.getDrawable(this, R.drawable.list_item_divider));
        listView.setDividerHeight(1);

        setSupportActionBar(findViewById(R.id.toolbar));

        init();
    }

    void init() {
        binding.uploadButton.setText(org.odk.collect.strings.R.string.send_selected_data);

        binding.toggleButton.setLongClickable(true);
        binding.toggleButton.setOnClickListener(v -> {
            if (!allSelected) {
                for (int i = 0; i < listView.getCount(); i++) {
                    multiSelectViewModel.select(listView.getItemIdAtPosition(i));
                }
            } else {
                multiSelectViewModel.unselectAll();
            }
        });
        binding.toggleButton.setOnLongClickListener(this);

        setupAdapter();

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

        getSupportLoaderManager().initLoader(AppListActivity.LOADER_ID, null, this);

        // Start observer that sets autoSendOngoing field based on AutoSendWorker status
        updateAutoSendStatus();
    }

    /**
     * Updates whether an auto-send job is ongoing.
     */
    private void updateAutoSendStatus() {
        // This shouldn't use WorkManager directly but it's likely this code will be removed when
        // we eventually move sending forms to a Foreground Service (rather than a blocking AsyncTask)
        String tag = ((FormUpdateAndInstanceSubmitScheduler) instanceSubmitScheduler).getAutoSendTag(projectsDataService.getCurrentProject().getUuid());
        LiveData<List<WorkInfo>> statuses = WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(tag);
        statuses.observe(this, workStatuses -> {
            if (workStatuses != null) {
                for (WorkInfo status : workStatuses) {
                    if (status.getState().equals(WorkInfo.State.RUNNING)) {
                        autoSendOngoing = true;
                        return;
                    }
                }
                autoSendOngoing = false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreSelectedSortingOrder();
        binding.uploadButton.setText(org.odk.collect.strings.R.string.send_selected_data);
    }

    private void uploadSelectedFiles(long[] instanceIds) {
        String server = settingsProvider.getUnprotectedSettings().getString(KEY_PROTOCOL);

        if (server.equalsIgnoreCase(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
            // if it's Sheets, start the Sheets uploader
            // first make sure we have a google account selected
            if (new PlayServicesChecker().isGooglePlayServicesAvailable(this)) {
                Intent i = new Intent(this, GoogleSheetsUploaderActivity.class);
                i.putExtra(FormFillingActivity.KEY_INSTANCES, instanceIds);
                startActivityForResult(i, INSTANCE_UPLOADER);
            } else {
                new PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(this);
            }
        } else {
            // otherwise, do the normal aggregate/other thing.
            Intent i = new Intent(this, InstanceUploaderActivity.class);
            i.putExtra(FormFillingActivity.KEY_INSTANCES, instanceIds);
            startActivityForResult(i, INSTANCE_UPLOADER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.instance_uploader_menu, menu);

        getMenuInflater().inflate(R.menu.form_list_menu, menu);
        final MenuItem sortItem = menu.findItem(R.id.menu_sort);
        final MenuItem searchItem = menu.findItem(R.id.menu_filter);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(org.odk.collect.strings.R.string.search));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterText = query;
                updateAdapter();
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterText = newText;
                updateAdapter();
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                sortItem.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                sortItem.setVisible(true);
                return true;
            }
        });

        if (isSearchBoxShown) {
            searchItem.expandActionView();
            searchView.setQuery(savedFilterText, false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        if (item.getItemId() == R.id.menu_preferences) {
            createPreferencesMenu();
            return true;
        } else if (item.getItemId() == R.id.menu_change_view) {
            showSentAndUnsentChoices();
            return true;
        }

        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        if (item.getItemId() == R.id.menu_sort) {
            new FormListSortingBottomSheetDialog(
                    this,
                    sortingOptions,
                    selectedSortingOrder,
                    selectedOption -> {
                        saveSelectedSortingOrder(selectedOption);
                        updateAdapter();
                    }
            ).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createPreferencesMenu() {
        Intent i = new Intent(this, ProjectPreferencesActivity.class);
        startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
        Cursor c = (Cursor) listView.getAdapter().getItem(position);
        boolean encryptedForm = !Boolean.parseBoolean(c.getString(c.getColumnIndex(DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE)));
        if (encryptedForm) {
            ToastUtils.showLongToast(this, org.odk.collect.strings.R.string.encrypted_form);
        } else {
            long instanceId = c.getLong(c.getColumnIndex(DatabaseInstanceColumns._ID));
            Intent intent = FormFillingIntentFactory.editInstanceIntent(this, projectsDataService.getCurrentProject().getUuid(), instanceId);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (searchView != null) {
            outState.putBoolean(IS_SEARCH_BOX_SHOWN, !searchView.isIconified());
            outState.putString(SEARCH_TEXT, String.valueOf(searchView.getQuery()));
        } else {
            Timber.e(new Error("Unexpected null search view (issue #1412)"));
        }

        outState.putBoolean(SHOW_ALL_MODE, showAllMode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_CANCELED) {
            multiSelectViewModel.unselectAll();
            return;
        }

        switch (requestCode) {
            // returns with a form path, start entry
            case INSTANCE_UPLOADER:
                if (intent.getBooleanExtra(FormFillingActivity.KEY_SUCCESS, false)) {
                    listView.clearChoices();
                    if (listAdapter.isEmpty()) {
                        finish();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void setupAdapter() {
        listAdapter = new InstanceUploaderAdapter(this, null, dbId -> {
            multiSelectViewModel.toggle(dbId);
        });

        listView.setAdapter(listAdapter);
    }

    private String getSortingOrderKey() {
        return INSTANCE_UPLOADER_LIST_SORTING_ORDER;
    }

    private void updateAdapter() {
        getSupportLoaderManager().restartLoader(AppListActivity.LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressBar();
        if (showAllMode) {
            return new CursorLoaderFactory(projectsDataService).createCompletedUndeletedInstancesCursorLoader(getFilterText(), getSortingOrder());
        } else {
            return new CursorLoaderFactory(projectsDataService).createFinalizedInstancesCursorLoader(getFilterText(), getSortingOrder());
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        hideProgressBarAndAllow();
        listAdapter.changeCursor(cursor);
        AppListActivity.toggleButtonLabel(findViewById(R.id.toggle_button), listView);

        if (listAdapter.isEmpty()) {
            findViewById(R.id.buttonholder).setVisibility(View.GONE);
        } else {
            findViewById(R.id.buttonholder).setVisibility(View.VISIBLE);
        }
        readyToSendViewModel.update();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        listAdapter.swapCursor(null);
    }

    @Override
    public boolean onLongClick(View v) {
        return showSentAndUnsentChoices();
    }

    /*
     * Create a dialog with options to save and exit, save, or quit without
     * saving
     */
    private boolean showSentAndUnsentChoices() {
        String[] items = {getString(org.odk.collect.strings.R.string.show_unsent_forms),
                getString(org.odk.collect.strings.R.string.show_sent_and_unsent_forms)};

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(org.odk.collect.strings.R.string.change_view))
                .setNeutralButton(getString(org.odk.collect.strings.R.string.cancel), (dialog, id) -> {
                    dialog.cancel();
                })
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0: // show unsent
                            showAllMode = false;
                            updateAdapter();
                            break;

                        case 1: // show all
                            showAllMode = true;
                            updateAdapter();
                            break;

                        case 2:// do nothing
                            break;
                    }
                }).create();
        alertDialog.show();
        return true;
    }

    private String getSortingOrder() {
        String sortingOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + DatabaseInstanceColumns.STATUS + " DESC";
        switch (getSelectedSortingOrder()) {
            case ApplicationConstants.SortingOrder.BY_NAME_ASC:
                sortingOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + DatabaseInstanceColumns.STATUS + " DESC";
                break;
            case ApplicationConstants.SortingOrder.BY_NAME_DESC:
                sortingOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + DatabaseInstanceColumns.STATUS + " DESC";
                break;
            case ApplicationConstants.SortingOrder.BY_DATE_ASC:
                sortingOrder = DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC";
                break;
            case ApplicationConstants.SortingOrder.BY_DATE_DESC:
                sortingOrder = DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
                break;
        }
        return sortingOrder;
    }

    private int getSelectedSortingOrder() {
        if (selectedSortingOrder == null) {
            restoreSelectedSortingOrder();
        }
        return selectedSortingOrder;
    }

    private void restoreSelectedSortingOrder() {
        selectedSortingOrder = settingsProvider.getUnprotectedSettings().getInt(getSortingOrderKey());
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBarAndAllow() {
        hideProgressBar();
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private CharSequence getFilterText() {
        return filterText != null ? filterText : "";
    }

    private void saveSelectedSortingOrder(int selectedStringOrder) {
        selectedSortingOrder = selectedStringOrder;
        settingsProvider.getUnprotectedSettings().save(getSortingOrderKey(), selectedStringOrder);
    }
}
