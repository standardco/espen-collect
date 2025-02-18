package org.espen.collect.android.database.itemsets

import org.espen.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.espen.collect.android.itemsets.FastExternalItemsetsRepository

class DatabaseFastExternalItemsetsRepository : FastExternalItemsetsRepository {

    override fun deleteAllByCsvPath(path: String) {
        ItemsetDbAdapter().open().use {
            it.delete(path)
        }
    }
}
