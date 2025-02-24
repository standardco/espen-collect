package org.espen.collect.android.database

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import org.espen.collect.android.database.forms.DatabaseFormColumns
import org.espen.collect.android.database.instances.DatabaseInstanceColumns
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.PathUtils.getAbsoluteFilePath
import org.odk.collect.shared.PathUtils.getRelativeFilePath
import java.lang.Boolean
import org.espen.collect.android.database.lookups.DatabaseLookupColumns
import org.odk.collect.lookup.LookUp

object DatabaseObjectMapper {

    @JvmStatic
    fun getValuesFromForm(form: Form, formsPath: String): ContentValues {
        val formFilePath = getRelativeFilePath(formsPath, form.formFilePath)
        val formMediaPath = form.formMediaPath?.let { getRelativeFilePath(formsPath, it) }

        val values = ContentValues()
        values.put(BaseColumns._ID, form.dbId)
        values.put(DatabaseFormColumns.DISPLAY_NAME, form.displayName)
        values.put(DatabaseFormColumns.DESCRIPTION, form.description)
        values.put(DatabaseFormColumns.JR_FORM_ID, form.formId)
        values.put(DatabaseFormColumns.JR_VERSION, form.version)
        values.put(DatabaseFormColumns.FORM_FILE_PATH, formFilePath)
        values.put(DatabaseFormColumns.SUBMISSION_URI, form.submissionUri)
        values.put(DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY, form.basE64RSAPublicKey)
        values.put(DatabaseFormColumns.MD5_HASH, form.mD5Hash)
        values.put(DatabaseFormColumns.FORM_MEDIA_PATH, formMediaPath)
        values.put(DatabaseFormColumns.LANGUAGE, form.language)
        values.put(DatabaseFormColumns.AUTO_SEND, form.autoSend)
        values.put(DatabaseFormColumns.DATE, form.date)
        values.put(DatabaseFormColumns.AUTO_DELETE, form.autoDelete)
        values.put(DatabaseFormColumns.GEOMETRY_XPATH, form.geometryXpath)
        values.put(DatabaseFormColumns.LAST_DETECTED_ATTACHMENTS_UPDATE_DATE, form.lastDetectedAttachmentsUpdateDate)
        values.put(DatabaseFormColumns.USES_ENTITIES, Boolean.toString(form.usesEntities()))
        return values
    }

    @JvmStatic
    fun getFormFromCurrentCursorPosition(
        cursor: Cursor,
        formsPath: String,
        cachePath: String
    ): Form? {
        val idColumnIndex = cursor.getColumnIndex(BaseColumns._ID)
        val displayNameColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.DISPLAY_NAME)
        val descriptionColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.DESCRIPTION)
        val jrFormIdColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.JR_FORM_ID)
        val jrVersionColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.JR_VERSION)
        val formFilePathColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.FORM_FILE_PATH)
        val submissionUriColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.SUBMISSION_URI)
        val base64RSAPublicKeyColumnIndex =
            cursor.getColumnIndex(DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY)
        val md5HashColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.MD5_HASH)
        val dateColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.DATE)
        val jrCacheFilePathColumnIndex =
            cursor.getColumnIndex(DatabaseFormColumns.JRCACHE_FILE_PATH)
        val formMediaPathColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.FORM_MEDIA_PATH)
        val languageColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.LANGUAGE)
        val autoSendColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.AUTO_SEND)
        val autoDeleteColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.AUTO_DELETE)
        val geometryXpathColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.GEOMETRY_XPATH)
        val deletedDateColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.DELETED_DATE)
        val lastDetectedAttachmentsUpdateDateColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)
        val usesEntitiesColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.USES_ENTITIES)
        return Form.Builder()
            .dbId(cursor.getLong(idColumnIndex))
            .displayName(cursor.getString(displayNameColumnIndex))
            .description(cursor.getString(descriptionColumnIndex))
            .formId(cursor.getString(jrFormIdColumnIndex))
            .version(cursor.getString(jrVersionColumnIndex))
            .formFilePath(
                getAbsoluteFilePath(
                    formsPath,
                    cursor.getString(formFilePathColumnIndex)
                )
            )
            .submissionUri(cursor.getString(submissionUriColumnIndex))
            .base64RSAPublicKey(cursor.getString(base64RSAPublicKeyColumnIndex))
            .md5Hash(cursor.getString(md5HashColumnIndex))
            .date(cursor.getLong(dateColumnIndex))
            .jrCacheFilePath(
                getAbsoluteFilePath(
                    cachePath,
                    cursor.getString(jrCacheFilePathColumnIndex)
                )
            )
            .formMediaPath(
                getAbsoluteFilePath(
                    formsPath,
                    cursor.getString(formMediaPathColumnIndex)
                )
            )
            .language(cursor.getString(languageColumnIndex))
            .autoSend(cursor.getString(autoSendColumnIndex))
            .autoDelete(cursor.getString(autoDeleteColumnIndex))
            .geometryXpath(cursor.getString(geometryXpathColumnIndex))
            .deleted(!cursor.isNull(deletedDateColumnIndex))
            .lastDetectedAttachmentsUpdateDate(if (cursor.isNull(lastDetectedAttachmentsUpdateDateColumnIndex)) null else cursor.getLong(lastDetectedAttachmentsUpdateDateColumnIndex))
            .usesEntities(Boolean.valueOf(cursor.getString(usesEntitiesColumnIndex)))
            .build()
    }

    @JvmStatic
    fun getInstanceFromValues(values: ContentValues): Instance? {
        return Instance.Builder()
            .dbId(values.getAsLong(BaseColumns._ID))
            .displayName(values.getAsString(DatabaseInstanceColumns.DISPLAY_NAME))
            .submissionUri(values.getAsString(DatabaseInstanceColumns.SUBMISSION_URI))
            .canEditWhenComplete(Boolean.parseBoolean(values.getAsString(DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE)))
            .instanceFilePath(values.getAsString(DatabaseInstanceColumns.INSTANCE_FILE_PATH))
            .formId(values.getAsString(DatabaseInstanceColumns.JR_FORM_ID))
            .formVersion(values.getAsString(DatabaseInstanceColumns.JR_VERSION))
            .status(values.getAsString(DatabaseInstanceColumns.STATUS))
            .lastStatusChangeDate(values.getAsLong(DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE))
            .deletedDate(values.getAsLong(DatabaseInstanceColumns.DELETED_DATE))
            .geometry(values.getAsString(DatabaseInstanceColumns.GEOMETRY))
            .geometryType(values.getAsString(DatabaseInstanceColumns.GEOMETRY_TYPE))
            .build()
    }

    @JvmStatic
    fun getInstanceFromCurrentCursorPosition(cursor: Cursor, instancesPath: String): Instance? {
        val dbId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
        val displayNameColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.DISPLAY_NAME)
        val submissionUriColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.SUBMISSION_URI)
        val canEditWhenCompleteIndex =
            cursor.getColumnIndex(DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE)
        val instanceFilePathIndex =
            cursor.getColumnIndex(DatabaseInstanceColumns.INSTANCE_FILE_PATH)
        val jrFormIdColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.JR_FORM_ID)
        val jrVersionColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.JR_VERSION)
        val statusColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.STATUS)
        val lastStatusChangeDateColumnIndex =
            cursor.getColumnIndex(DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE)
        val deletedDateColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.DELETED_DATE)
        val geometryTypeColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.GEOMETRY_TYPE)
        val geometryColumnIndex = cursor.getColumnIndex(DatabaseInstanceColumns.GEOMETRY)
        val databaseIdIndex = cursor.getColumnIndex(BaseColumns._ID)
        val canDeleteBeforeSendIndex =
            cursor.getColumnIndex(DatabaseInstanceColumns.CAN_DELETE_BEFORE_SEND)
        return Instance.Builder()
            .dbId(dbId)
            .displayName(cursor.getString(displayNameColumnIndex))
            .submissionUri(cursor.getString(submissionUriColumnIndex))
            .canEditWhenComplete(Boolean.valueOf(cursor.getString(canEditWhenCompleteIndex)))
            .instanceFilePath(
                getAbsoluteFilePath(
                    instancesPath,
                    cursor.getString(instanceFilePathIndex)
                )
            )
            .formId(cursor.getString(jrFormIdColumnIndex))
            .formVersion(cursor.getString(jrVersionColumnIndex))
            .status(cursor.getString(statusColumnIndex))
            .lastStatusChangeDate(cursor.getLong(lastStatusChangeDateColumnIndex))
            .deletedDate(
                if (cursor.isNull(deletedDateColumnIndex)) {
                    null
                } else {
                    cursor.getLong(
                        deletedDateColumnIndex
                    )
                }
            )
            .geometryType(cursor.getString(geometryTypeColumnIndex))
            .geometry(cursor.getString(geometryColumnIndex))
            .dbId(cursor.getLong(databaseIdIndex))
            .canDeleteBeforeSend(Boolean.valueOf(cursor.getString(canDeleteBeforeSendIndex)))
            .build()
    }

    @JvmStatic
    fun getValuesFromInstance(instance: Instance, instancesPath: String): ContentValues {
        val values = ContentValues()
        values.put(BaseColumns._ID, instance.dbId)
        values.put(DatabaseInstanceColumns.DISPLAY_NAME, instance.displayName)
        values.put(DatabaseInstanceColumns.SUBMISSION_URI, instance.submissionUri)
        values.put(
            DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE,
            Boolean.toString(instance.canEditWhenComplete())
        )
        values.put(
            DatabaseInstanceColumns.INSTANCE_FILE_PATH,
            getRelativeFilePath(instancesPath, instance.instanceFilePath)
        )
        values.put(DatabaseInstanceColumns.JR_FORM_ID, instance.formId)
        values.put(DatabaseInstanceColumns.JR_VERSION, instance.formVersion)
        values.put(DatabaseInstanceColumns.STATUS, instance.status)
        values.put(DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE, instance.lastStatusChangeDate)
        values.put(DatabaseInstanceColumns.DELETED_DATE, instance.deletedDate)
        values.put(DatabaseInstanceColumns.GEOMETRY, instance.geometry)
        values.put(DatabaseInstanceColumns.GEOMETRY_TYPE, instance.geometryType)
        values.put(
            DatabaseInstanceColumns.CAN_DELETE_BEFORE_SEND,
            Boolean.toString(instance.canDeleteBeforeSend())
        )

        return values
    }

    @JvmStatic
    fun getValuesFromLookUp(lookup: LookUp, instancesPath: String): ContentValues {
        val values = ContentValues()
        values.put(BaseColumns._ID, lookup.dbId)
        values.put(DatabaseLookupColumns.INSTANCE_PATH, getRelativeFilePath(instancesPath, lookup.instancePath))
        values.put(DatabaseLookupColumns.COLUMN_1, lookup.column1)
        values.put(DatabaseLookupColumns.COLUMN_2, lookup.column2)
        values.put(DatabaseLookupColumns.COLUMN_3, lookup.column3)
        values.put(DatabaseLookupColumns.COLUMN_4, lookup.column4)
        values.put(DatabaseLookupColumns.COLUMN_5, lookup.column5)
        values.put(DatabaseLookupColumns.COLUMN_6, lookup.column6)
        values.put(DatabaseLookupColumns.COLUMN_7, lookup.column7)
        values.put(DatabaseLookupColumns.COLUMN_8, lookup.column8)
        values.put(DatabaseLookupColumns.COLUMN_9, lookup.column9)
        values.put(DatabaseLookupColumns.COLUMN_10, lookup.column10)

        return values
    }

    @JvmStatic
    fun getLookUpFromCurrentCursorPosition(cursor: Cursor, instancesPath: String): LookUp? {
        val dbId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
        val instanceFilePathIndex = cursor.getColumnIndex(DatabaseLookupColumns.INSTANCE_PATH)
        val column1Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_1)
        val column2Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_2)
        val column3Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_3)
        val column4Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_4)
        val column5Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_5)
        val column6Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_6)
        val column7Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_7)
        val column8Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_8)
        val column9Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_9)
        val column10Index = cursor.getColumnIndex(DatabaseLookupColumns.COLUMN_10)
        val look = LookUp();
        look.setDBId(dbId);
        look.instancePath = getAbsoluteFilePath( instancesPath, cursor.getString(instanceFilePathIndex) )
        look.column1 = cursor.getString(column1Index);
        look.column2 = cursor.getString(column2Index);
        look.column3 = cursor.getString(column3Index);
        look.column4 = cursor.getString(column4Index);
        look.column5 = cursor.getString(column5Index);
        look.column6 = cursor.getString(column6Index);
        look.column7 = cursor.getString(column7Index);
        look.column8 = cursor.getString(column8Index);
        look.column9 = cursor.getString(column9Index);
        look.column10 = cursor.getString(column10Index);

        return look;
    }

    @JvmStatic
    fun getLookUpFromValues(values: ContentValues): LookUp? {
        val look = LookUp();
        look.setDBId(values.getAsLong(BaseColumns._ID));
        look.instancePath = values.getAsString(DatabaseLookupColumns.INSTANCE_PATH);
        look.column1 = values.getAsString(DatabaseLookupColumns.COLUMN_1);
        look.column2 = values.getAsString(DatabaseLookupColumns.COLUMN_2);
        look.column3 = values.getAsString(DatabaseLookupColumns.COLUMN_3);
        look.column4 = values.getAsString(DatabaseLookupColumns.COLUMN_4);
        look.column5 = values.getAsString(DatabaseLookupColumns.COLUMN_5);
        look.column6 = values.getAsString(DatabaseLookupColumns.COLUMN_6);
        look.column7 = values.getAsString(DatabaseLookupColumns.COLUMN_7);
        look.column8 = values.getAsString(DatabaseLookupColumns.COLUMN_8);
        look.column9 = values.getAsString(DatabaseLookupColumns.COLUMN_9);
        look.column10 = values.getAsString(DatabaseLookupColumns.COLUMN_10);
        return look;
    }
}
