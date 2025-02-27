package org.espen.collect.android.database.lookups;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import org.odk.collect.db.sqlite.DatabaseConnection;
import org.espen.collect.android.database.DatabaseConstants;
import org.espen.collect.android.database.DatabaseObjectMapper;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.lookup.LookUpRepository;
import org.odk.collect.lookup.LookUp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static android.provider.BaseColumns._ID;
import static org.espen.collect.android.database.DatabaseConstants.FORMS_TABLE_NAME;
import static org.espen.collect.android.database.DatabaseConstants.LOOKUP_TABLE_NAME;
import static org.espen.collect.android.database.DatabaseObjectMapper.getLookUpFromCurrentCursorPosition;

//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.DELETED_DATE;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.DISPLAY_NAME;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY_TYPE;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.INSTANCE_FILE_PATH;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.JR_FORM_ID;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.JR_VERSION;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.STATUS;
//import static org.espen.collect.android.database.instances.DatabaseInstanceColumns.SUBMISSION_URI;
import static org.espen.collect.android.database.DatabaseObjectMapper.getValuesFromLookUp;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_1;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_10;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_2;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_3;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_4;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_5;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_6;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_7;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_8;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_9;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.INSTANCE_PATH;

/**
 * Mediates between {@link Instance} objects and the underlying SQLite database that stores them.
 */
public final class DatabaseLookupRepository implements LookUpRepository {

    private final DatabaseConnection databaseConnection;
    private final Supplier<Long> clock;
    private final String instancesPath;

    public DatabaseLookupRepository(Context context, String dbPath, String instancesPath, Supplier<Long> clock) {
        this.databaseConnection = new DatabaseConnection(
                context,
                dbPath,
                DatabaseConstants.LOOKUP_DATABASE_NAME,
                new LookupDatabaseMigrator(),
                DatabaseConstants.LOOKUP_DATABASE_VERSION
        );

        this.clock = clock;
        this.instancesPath = instancesPath;
    }

    @Override
    public LookUp get(Long databaseId) {
        String selection = _ID + "=?";
        String[] selectionArgs = {Long.toString(databaseId)};

        try (Cursor cursor = query(null, selection, selectionArgs, null)) {
            List<LookUp> result = getLookUpsFromCursor(cursor, instancesPath);
            return !result.isEmpty() ? result.get(0) : null;
        }
    }

//    @Override
//    public LookUp getOneByPath(String instancePath) {
//        String selection = INSTANCE_PATH + "=?";
//        String[] args = {getRelativeFilePath(instancesPath, instancePath)};
//        try (Cursor cursor = query(null, selection, args, null)) {
//            List<LookUp> instances = getInstancesFromCursor(cursor, instancesPath);
//            if (instances.size() == 1) {
//                return instances.get(0);
//            } else {
//                return null;
//            }
//        }
//    }

//    @Override
//    public List<LookUp> getAll() {
//        try (Cursor cursor = query(null, null, null, null)) {
//            return getInstancesFromCursor(cursor, instancesPath);
//        }
//    }
//
//    @Override
//    public List<LookUp> getAllNotDeleted() {
//        try (Cursor cursor = query(null, DELETED_DATE + " IS NULL ", null, null)) {
//            return getInstancesFromCursor(cursor, instancesPath);
//        }
//    }
//
//    @Override
//    public List<LookUp> getAllByStatus(String... status) {
//        List<LookUp> lookups = Lis;
////        try (Cursor instancesCursor = getCursorForAllByStatus(status)) {
////            return getInstancesFromCursor(instancesCursor, instancesPath);
////        }
//
//    }

//    @Override
//    public int getCountByStatus(String... status) {
//        try (Cursor cursorForAllByStatus = getCursorForAllByStatus(status)) {
//            return cursorForAllByStatus.getCount();
//        }
//    }
//
//
//    @Override
//    public List<Instance> getAllByFormId(String formId) {
//        try (Cursor c = query(null, JR_FORM_ID + " = ?", new String[]{formId}, null)) {
//            return getInstancesFromCursor(c, instancesPath);
//        }
//    }

//    @Override
//    public List<Instance> getAllNotDeletedByFormIdAndVersion(String jrFormId, String jrVersion) {
//        if (jrVersion != null) {
//            try (Cursor cursor = query(null, JR_FORM_ID + " = ? AND " + JR_VERSION + " = ? AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId, jrVersion}, null)) {
//                return getInstancesFromCursor(cursor, instancesPath);
//            }
//        } else {
//            try (Cursor cursor = query(null, JR_FORM_ID + " = ? AND " + JR_VERSION + " IS NULL AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId}, null)) {
//                return getInstancesFromCursor(cursor, instancesPath);
//            }
//        }
//    }

    @Override
    public void delete(Long id) {
        SQLiteDatabase writableDatabase = databaseConnection.getWritableDatabase();
        writableDatabase.delete(DatabaseConstants.LOOKUP_TABLE_NAME,
                _ID + "=?",
                new String[]{String.valueOf(id)});

    }
//
//    @Override
//    public void deleteAll() {
//        List<LookUp> lookups = getAll();
//
//        databaseConnection.getWriteableDatabase().delete(
//                LOOKUP_TABLE_NAME,
//                null,
//                null
//        );
//
//        for (LookUp lookup : lookups) {
//            deleteInstanceFiles(lookup);
//        }
//    }

    @Override
    public void save(LookUp lookup) {
        if (lookup.getDbId() == null) {
            long insertId = insert(DatabaseObjectMapper.getValuesFromLookUp(lookup, instancesPath));
        } else {
            update(lookup.getDbId(), DatabaseObjectMapper.getValuesFromLookUp(lookup, instancesPath));
        }
    }

 //   @Override
 //   public void deleteWithLogging(Long id) {
//        ContentValues values = new ContentValues();
//        values.putNull(GEOMETRY);
//        values.putNull(GEOMETRY_TYPE);
//        values.put(DELETED_DATE, clock.get());
//        update(id, values);
//
//        Instance instance = get(id);
//        deleteInstanceFiles(instance);
//    }

    public Cursor rawQuery(String[] projection, String selection, String[] selectionArgs, String sortOrder, String groupBy) {
        return query(projection, selection, selectionArgs, sortOrder);
    }
    public List<String> getBySearchQuery(String projectionColumn, String selection, String[] selectionArgs){
        String[] arrProjectColumn = {projectionColumn};
        List<String> result = new ArrayList<>();
        try (Cursor cursor = query(arrProjectColumn, selection, selectionArgs, null)) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                var columnIndex = cursor.getColumnIndex(projectionColumn);
                var value = cursor.getString(columnIndex);
                if(!result.contains(value)){
                    result.add(value);
                }
            }
            cursor.close();
        }
        return result;
    }
//    private Cursor getCursorForAllByStatus(String[] status) {
//        StringBuilder selection = new StringBuilder(STATUS + "=?");
//        for (int i = 1; i < status.length; i++) {
//            selection.append(" or ").append(STATUS).append("=?");
//        }
//
//        return query(null, selection.toString(), status, null);
//    }

    private Cursor query(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase readableDatabase = databaseConnection.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseConstants.LOOKUP_TABLE_NAME);

        if (projection == null) {
            /*
             For some reason passing null as the projection doesn't always give us all the
             columns so we hardcode them here so it's explicit that we need these all back.
             */
            projection = new String[]{
                    _ID, INSTANCE_PATH
                    , COLUMN_1, COLUMN_2,
                    COLUMN_3, COLUMN_4, COLUMN_5, COLUMN_6, COLUMN_7, COLUMN_8, COLUMN_9, COLUMN_10
            };
        }
//        var cursor = qb.query(readableDatabase, projection, null, null, null, null, null);
//        cursor.moveToPosition(-1);
//        while (cursor.moveToNext()) {
//            var columnIndex = cursor.getColumnIndex(projection[0]);
//            var value = cursor.getString(columnIndex);
//
//        }
        return qb.query(readableDatabase, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private long insert(ContentValues values) {
        SQLiteDatabase writableDatabase = databaseConnection.getWritableDatabase();
        return writableDatabase.insertOrThrow(
                DatabaseConstants.LOOKUP_TABLE_NAME,
                null,
                values
        );
    }

    private void update(Long instanceId, ContentValues values) {
        SQLiteDatabase writableDatabase = databaseConnection.getWritableDatabase();
        writableDatabase.update(
                DatabaseConstants.LOOKUP_TABLE_NAME,
                values,
                _ID + "=?",
                new String[]{instanceId.toString()}
        );
    }

    private void deleteInstanceFiles(LookUp lookup) {
        //DirectoryUtils.deleteDirectory(new File(lookup.getInstanceFilePath()).getParentFile());
    }

    private static List<LookUp> getLookUpsFromCursor(Cursor cursor, String instancesPath) {
        List<LookUp> lookups = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            LookUp lookup = DatabaseObjectMapper.getLookUpFromCurrentCursorPosition(cursor, instancesPath);
            lookups.add(lookup);
        }

        return lookups;
    }
}
