package org.espen.collect.android.preferences.screens

import android.content.Context
import org.espen.collect.android.geo.MapConfiguratorProvider
import org.espen.collect.android.preferences.CaptionedListPreference
import org.espen.collect.android.utilities.FileUtils
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.layers.ReferenceLayer
import org.odk.collect.maps.layers.ReferenceLayerRepository
import java.io.File
import java.util.Collections

object ReferenceLayerPreferenceUtils {

    /** Sets up the contents of the reference layer selection dialog.  */
    fun populateReferenceLayerPref(
        context: Context,
        referenceLayerRepository: ReferenceLayerRepository,
        referenceLayerPref: org.espen.collect.android.preferences.CaptionedListPreference
    ) {
        val cftor = org.espen.collect.android.geo.MapConfiguratorProvider.getConfigurator()
        val items: MutableList<org.espen.collect.android.preferences.CaptionedListPreference.Item> = ArrayList()
        items.add(org.espen.collect.android.preferences.CaptionedListPreference.Item(null, context.getString(org.odk.collect.strings.R.string.none), ""))
        val supportedLayerFiles = getSupportedLayerFiles(cftor, referenceLayerRepository)

        for ((id, file) in supportedLayerFiles) {
            val path = org.espen.collect.android.utilities.FileUtils.expandAndroidStoragePath(file.path)
            val name = cftor.getDisplayName(File(file.absolutePath))
            items.add(org.espen.collect.android.preferences.CaptionedListPreference.Item(id, name, path))
        }

        // Sort by display name, then by path for files with identical names.
        Collections.sort(items) { a: org.espen.collect.android.preferences.CaptionedListPreference.Item, b: org.espen.collect.android.preferences.CaptionedListPreference.Item ->
            if (a.value == null != (b.value == null)) {
                // one or the other is null
                return@sort if (a.value == null) -1 else 1
            }

            if (!a.label.equals(b.label, ignoreCase = true)) {
                return@sort a.label.compareTo(b.label, ignoreCase = true)
            }

            if (a.label != b.label) {
                return@sort a.label.compareTo(b.label)
            }

            if (a.value != null && b.value != null) {
                return@sort org.espen.collect.android.utilities.FileUtils.comparePaths(a.value, b.value)
            }

            0 // both a.value and b.value are null
        }

        if (referenceLayerPref.value != null && referenceLayerRepository.get(referenceLayerPref.value) == null) {
            referenceLayerPref.value = null
        }

        referenceLayerPref.setItems(items)
        referenceLayerPref.updateContent()
    }

    /** Gets the list of layer data files supported by the current MapConfigurator.  */
    private fun getSupportedLayerFiles(
        cftor: MapConfigurator,
        referenceLayerRepository: ReferenceLayerRepository
    ): List<ReferenceLayer> {
        val supportedLayers: MutableList<ReferenceLayer> = ArrayList()
        for (layer in referenceLayerRepository.getAll()) {
            if (cftor.supportsLayer(layer.file)) {
                supportedLayers.add(layer)
            }
        }
        return supportedLayers
    }
}
