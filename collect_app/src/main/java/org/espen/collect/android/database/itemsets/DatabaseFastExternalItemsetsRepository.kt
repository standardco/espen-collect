package org.espen.collect.android.database.itemsets

import org.espen.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.espen.collect.android.itemsets.FastExternalItemsetsRepository

class DatabaseFastExternalItemsetsRepository : FastExternalItemsetsRepository {

    override fun deleteAllByCsvPath(path: String) {
        org.espen.collect.android.fastexternalitemset.ItemsetDbAdapter().open().use {
            it.delete(path)
        }
    }
}
