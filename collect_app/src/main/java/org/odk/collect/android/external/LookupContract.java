/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.external;

import android.net.Uri;

import org.odk.collect.android.database.forms.DatabaseFormColumns;

/**
 * Contract between the Lookup provider and applications. Contains definitions for the supported
 * URIs. Data columns are defined at {@link DatabaseLookupColumns}.
 * <p>
 */
public final class LookupContract {

//    static final String AUTHORITY = "org.odk.collect.android.provider.odk.forms";
//    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";
//    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.form";
    static final String AUTHORITY =  "com.espen.provider.odk.v2.lookup";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.espen.lookup";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.espen.lookup";

    /**
     * The content:// style URL for accessing Forms.
     */
    public static Uri getUri(String projectId, Long formDbId) {
        return Uri.parse("content://" + AUTHORITY + "/lookups/" + formDbId + "?projectId=" + projectId);
    }

    public static Uri getUri(String projectId) {
        return Uri.parse("content://" + AUTHORITY + "/lookups?projectId=" + projectId);
    }

    @Deprecated
    public static Uri getContentNewestFormsByFormIdUri(String projectId) {
        return Uri.parse("content://" + AUTHORITY + "/newest_lookups_by_lookup_id?projectId=" + projectId);
    }

    private LookupContract() {
    }
}
