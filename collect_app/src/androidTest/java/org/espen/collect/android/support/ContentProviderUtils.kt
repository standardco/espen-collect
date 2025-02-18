package org.espen.collect.android.support

import android.app.Application
import android.provider.BaseColumns
import androidx.test.core.app.ApplicationProvider
import org.espen.collect.android.database.forms.DatabaseFormColumns
import org.espen.collect.android.database.instances.DatabaseInstanceColumns
import org.espen.collect.android.external.FormsContract
import org.espen.collect.android.external.InstancesContract

object ContentProviderUtils {

    fun getFormDatabaseId(projectId: String, formId: String): Long {
        val contentResolver =
            ApplicationProvider.getApplicationContext<Application>().contentResolver
        val uri = FormsContract.getUri(projectId)
        return contentResolver.query(uri, null, null, null, null, null).use {
            if (it != null) {
                var dbId: Long? = null
                while (it.moveToNext()) {
                    if (it.getString(it.getColumnIndex(DatabaseFormColumns.JR_FORM_ID)) == formId) {
                        dbId = it.getLong(it.getColumnIndex(BaseColumns._ID))
                    }
                }

                dbId ?: throw IllegalArgumentException("Form does not exist!")
            } else {
                throw RuntimeException("Null cursor!")
            }
        }
    }

    fun getInstanceDatabaseId(projectId: String, formId: String): Long {
        val contentResolver =
            ApplicationProvider.getApplicationContext<Application>().contentResolver
        val uri = InstancesContract.getUri(projectId)
        return contentResolver.query(uri, null, null, null, null, null).use {
            if (it != null) {
                var dbId: Long? = null
                while (it.moveToNext()) {
                    if (it.getString(it.getColumnIndex(DatabaseInstanceColumns.JR_FORM_ID)) == formId) {
                        dbId = it.getLong(it.getColumnIndex(BaseColumns._ID))
                    }
                }

                dbId ?: throw IllegalArgumentException("Form does not exist!")
            } else {
                throw RuntimeException("Null cursor!")
            }
        }
    }
}
