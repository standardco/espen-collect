package org.espen.collect.android.database;

public final class DatabaseConstants {

    public static final String FORMS_DATABASE_NAME = "forms.db";
    public static final String FORMS_TABLE_NAME = "forms";
    // Please always test upgrades manually when you change this value
    public static final int FORMS_DATABASE_VERSION = 14;

    public static final String INSTANCES_DATABASE_NAME = "instances.db";
    public static final String INSTANCES_TABLE_NAME = "instances";
    // Please always test upgrades manually when you change this value
    public static final int INSTANCES_DATABASE_VERSION = 8;

    public static final String SAVEPOINTS_DATABASE_NAME = "savepoints.db";
    public static final String SAVEPOINTS_TABLE_NAME = "savepoints";
    // Please always test upgrades manually when you change this value
    public static final int SAVEPOINTS_DATABASE_VERSION = 1;

    public static final String LOOKUP_DATABASE_NAME = "lookups.db";
    public static final String LOOKUP_TABLE_NAME = "lookups";
    public static final int LOOKUP_DATABASE_VERSION = 5;
    private DatabaseConstants() {

    }
}
