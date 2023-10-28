package org.espen.collect.android.database.lookups;

import android.database.sqlite.SQLiteDatabase;

import org.espen.collect.android.database.DatabaseConstants;
import org.espen.collect.android.database.DatabaseMigrator;
import org.espen.collect.android.database.instances.DatabaseInstanceColumns;
import org.espen.collect.android.database.DatabaseMigrator;
import org.espen.collect.android.database.instances.DatabaseInstanceColumns;
import org.espen.collect.android.utilities.SQLiteUtils;

import java.util.Arrays;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static org.espen.collect.android.database.DatabaseConstants.LOOKUP_DATABASE_NAME;
import static org.espen.collect.android.database.DatabaseConstants.LOOKUP_TABLE_NAME;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.INSTANCE_PATH;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_1;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_2;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_3;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_4;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_5;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_6;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_7;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_8;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_9 ;
import static org.espen.collect.android.database.lookups.DatabaseLookupColumns.COLUMN_10;


public class LookupDatabaseMigrator implements DatabaseMigrator {
    private static final String[] COLUMN_NAMES_V5 = {_ID, INSTANCE_PATH, COLUMN_1, COLUMN_2,
            COLUMN_3, COLUMN_4, COLUMN_5, COLUMN_6, COLUMN_7, COLUMN_8, COLUMN_9, COLUMN_10};
    public static final String[] CURRENT_VERSION_COLUMN_NAMES = COLUMN_NAMES_V5;
    public void onCreate(SQLiteDatabase db) {
        createInstancesTableV5(db, DatabaseConstants.LOOKUP_DATABASE_NAME);
        upgradeToVersion5(db);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    public void onUpgrade(SQLiteDatabase db, int oldVersion) {
//        Timber.w("Instances db upgrade from version: %s", oldVersion);
//        switch (oldVersion) {
//
//            case 5:
//                upgradeToVersion5(db);
//                // Remember to bump the database version number in {@link org.espen.collect.android.database.DatabaseConstants}
//                // upgradeToVersion7(db);
//            default:
//                Timber.i("Unknown version %d", oldVersion);
//        }
    }

    public void onDowngrade(SQLiteDatabase db) {
//        String temporaryTableName = LOOKUP_TABLE_NAME + "_tmp";
//        createInstancesTableV5(db, temporaryTableName);
//        dropObsoleteColumns(db, CURRENT_VERSION_COLUMN_NAMES, temporaryTableName);
    }


    /**
     * Upgrade to version 5. Prior versions of the instances table included a {@code displaySubtext}
     * column which was redundant with the {@link DatabaseInstanceColumns#STATUS} and
     * {@link DatabaseInstanceColumns#LAST_STATUS_CHANGE_DATE} columns and included
     * unlocalized text. Version 5 removes this column.
     */
    private void upgradeToVersion5(SQLiteDatabase db) {
        String temporaryTableName = DatabaseConstants.LOOKUP_TABLE_NAME;

        // onDowngrade in EspenCollect v1.22 always failed to clean up the temporary table so remove it now.
        // Going from v1.23 to v1.22 and back to v1.23 will result in instance status information
        // being lost.
        //SQLiteUtils.dropTable(db, temporaryTableName);

        createInstancesTableV5(db, temporaryTableName);
        //dropObsoleteColumns(db, COLUMN_NAMES_V5, temporaryTableName);
    }

    /**
     * Use the existing temporary table with the provided name to only keep the given relevant
     * columns, dropping all others.
     *
     * NOTE: the temporary table with the name provided is dropped.
     *
     * The move and copy strategy is used to overcome the fact that SQLITE does not directly support
     * removing a column. See https://sqlite.org/lang_altertable.html
     *
     * @param db                    the database to operate on
     * @param relevantColumns       the columns relevant to the current version
     * @param temporaryTableName    the name of the temporary table to use and then drop
     */
    private void dropObsoleteColumns(SQLiteDatabase db, String[] relevantColumns, String temporaryTableName) {
        List<String> columns = SQLiteUtils.getColumnNames(db, DatabaseConstants.LOOKUP_TABLE_NAME);
        columns.retainAll(Arrays.asList(relevantColumns));
        String[] columnsToKeep = columns.toArray(new String[0]);

        SQLiteUtils.copyRows(db, DatabaseConstants.LOOKUP_TABLE_NAME, columnsToKeep, temporaryTableName);
        SQLiteUtils.dropTable(db, DatabaseConstants.LOOKUP_TABLE_NAME);
        SQLiteUtils.renameTable(db, temporaryTableName, DatabaseConstants.LOOKUP_TABLE_NAME);
    }

    private void createInstancesTableV5(SQLiteDatabase db, String name) {
//        db.execSQL("CREATE TABLE IF NOT EXISTS " + name + " ("
//                + _ID + " integer primary key, "
//                + DISPLAY_NAME + " text not null, "
//                + SUBMISSION_URI + " text, "
//                + CAN_EDIT_WHEN_COMPLETE + " text, "
//                + INSTANCE_FILE_PATH + " text not null, "
//                + JR_FORM_ID + " text not null, "
//                + JR_VERSION + " text, "
//                + STATUS + " text not null, "
//                + LAST_STATUS_CHANGE_DATE + " date not null, "
//                + DELETED_DATE + " date );");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseConstants.LOOKUP_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + INSTANCE_PATH + " text not null, "
                + COLUMN_1 + " text, "
                + COLUMN_2 + " text, "
                + COLUMN_3 + " text, "
                + COLUMN_4 + " text, "
                + COLUMN_5 + " text, "
                + COLUMN_6 + " text, "
                + COLUMN_7 + " text, "
                + COLUMN_8 + " text, "
                + COLUMN_9 + " text, "
                + COLUMN_10 + " text);");
    }
}
