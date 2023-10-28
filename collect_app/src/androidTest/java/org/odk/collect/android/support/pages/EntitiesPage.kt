package org.espen.collect.android.support.pages

class EntitiesPage : Page<EntitiesPage>() {

    override fun assertOnPage(): EntitiesPage {
        assertToolbarTitle(org.odk.collect.strings.R.string.entities_title)
        return this
    }

    fun clickOnDataset(datasetName: String): DatasetPage {
        clickOnText(datasetName)
        return DatasetPage(datasetName)
    }
}
