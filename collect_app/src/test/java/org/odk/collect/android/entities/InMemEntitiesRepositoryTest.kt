package org.espen.collect.android.entities

import org.odk.collect.entities.EntitiesRepository

class InMemEntitiesRepositoryTest : EntitiesRepositoryTest() {

    override fun buildSubject(): EntitiesRepository {
        return InMemEntitiesRepository()
    }
}
