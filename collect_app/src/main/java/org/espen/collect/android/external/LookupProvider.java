/*
 * Copyright (C) 2007 The Android Open Source Project
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

package org.espen.collect.android.external;

import static android.provider.BaseColumns._ID;

import static org.espen.collect.android.database.DatabaseObjectMapper.getLookUpFromCurrentCursorPosition;
import static org.espen.collect.android.database.DatabaseObjectMapper.getLookUpFromValues;
import static org.espen.collect.android.database.DatabaseObjectMapper.getValuesFromLookUp;

import static org.espen.collect.android.external.LookupContract.getUri;
import static org.espen.collect.android.external.LookupContract.CONTENT_TYPE;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.espen.collect.android.analytics.AnalyticsEvents;
import org.espen.collect.android.analytics.AnalyticsUtils;
import org.espen.collect.android.dao.CursorLoaderFactory;
import org.espen.collect.android.database.DatabaseObjectMapper;
import org.espen.collect.android.database.lookups.DatabaseLookupRepository;
import org.espen.collect.android.utilities.FormsRepositoryProvider;
import org.espen.collect.android.utilities.LookUpRepositoryProvider;
import org.espen.collect.android.database.lookups.DatabaseLookupRepository;
import org.espen.collect.android.injection.DaggerUtils;
import org.espen.collect.android.storage.StoragePathProvider;
import org.espen.collect.android.storage.StorageSubdirectory;
import org.espen.collect.android.utilities.LookUpRepositoryProvider;
import org.odk.collect.lookup.LookUp;
import org.odk.collect.lookup.LookUpRepository;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.settings.SettingsProvider;

import javax.inject.Inject;

//import com.espen.database.ODKSQLiteOpenHelper;
//import com.espen.provider.LookupProviderAPI.LookupColumns;
//import com.espen.utilities.FileUtils;

/**
 * 
 */
public class LookupProvider extends ContentProvider {

    private static final int LOOKUP = 1;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    @Inject
    LookUpRepositoryProvider lookupsRepositoryProvider;

    @Inject
    ProjectsRepository projectsRepository;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    StoragePathProvider storagePathProvider;

    @Override
    public boolean onCreate() {
        return true;
    }
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        DaggerUtils.getComponent(getContext()).inject(this);

        String projectId = getProjectId(uri);

        // We only want to log external calls to the content provider
        if (uri.getQueryParameter(CursorLoaderFactory.INTERNAL_QUERY_PARAM) == null) {
            logServerEvent(projectId, AnalyticsEvents.LOOKUP_PROVIDER_QUERY);
        }

        Cursor c;
        switch (URI_MATCHER.match(uri)) {
            case LOOKUP:
                c = dbQuery(projectId, projection, selection, selectionArgs, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case LOOKUP:
                return LookupContract.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        DaggerUtils.getComponent(getContext()).inject(this);

        String projectId = getProjectId(uri);
        logServerEvent(projectId, AnalyticsEvents.LOOKUP_PROVIDER_INSERT);

        // Validate the requested uri
        if (URI_MATCHER.match(uri) != LOOKUP) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        LookUp lookup = DatabaseObjectMapper.getLookUpFromValues(initialValues);
        lookupsRepositoryProvider.create(projectId).save(lookup);
        return LookupContract.getUri(projectId, lookup.getDbId());
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        DaggerUtils.getComponent(getContext()).inject(this);

        String projectId = getProjectId(uri);
        logServerEvent(projectId, AnalyticsEvents.LOOKUP_PROVIDER_DELETE);

        int count;

        switch (URI_MATCHER.match(uri)) {
            case LOOKUP:
                try (Cursor cursor = dbQuery(projectId, new String[]{_ID}, where, whereArgs, null)) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndex(_ID));
                        lookupsRepositoryProvider.create(projectId).delete(id);
                    }

                    count = cursor.getCount();
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        DaggerUtils.getComponent(getContext()).inject(this);

        String projectId = getProjectId(uri);
        logServerEvent(projectId, AnalyticsEvents.LOOKUP_PROVIDER_UPDATE);

        LookUpRepository instancesRepository = lookupsRepositoryProvider.create(projectId);
        String instancesPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.LOOKUPS, projectId);

        int count;

        switch (URI_MATCHER.match(uri)) {
            case LOOKUP:
                try (Cursor cursor = dbQuery(projectId, null, where, whereArgs, null)) {
                    while (cursor.moveToNext()) {
                        LookUp lookup = DatabaseObjectMapper.getLookUpFromCurrentCursorPosition(cursor, instancesPath);
                        ContentValues existingValues = DatabaseObjectMapper.getValuesFromLookUp(lookup, instancesPath);

                        existingValues.putAll(values);
                        instancesRepository.save(DatabaseObjectMapper.getLookUpFromValues(existingValues));
                    }

                    count = cursor.getCount();
                }

                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
    private Cursor dbQuery(String projectId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return ((DatabaseLookupRepository) lookupsRepositoryProvider.create(projectId)).rawQuery(projection, selection, selectionArgs, sortOrder, null);
    }

    private String getProjectId(@NonNull Uri uri) {
        String queryParam = uri.getQueryParameter("projectId");

        if (queryParam != null) {
            return queryParam;
        } else {
            return projectsRepository.getAll().get(0).getUuid();
        }
    }

    private void logServerEvent(String projectId, String event) {
        AnalyticsUtils.logServerEvent(event, settingsProvider.getUnprotectedSettings(projectId));
    }


    static {
        URI_MATCHER.addURI(LookupContract.AUTHORITY, "lookups", LOOKUP);
    }
}
