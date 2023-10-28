/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.espen.collect.android.preferences.screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import org.espen.collect.android.R
import org.espen.collect.android.geo.MapConfiguratorProvider
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.preferences.CaptionedListPreference
import org.espen.collect.android.preferences.dialogs.ReferenceLayerPreferenceDialog
import org.espen.collect.android.preferences.screens.ReferenceLayerPreferenceUtils.populateReferenceLayerPref
import org.espen.collect.androidshared.ui.PrefUtils
import org.espen.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.keys.ProjectKeys.CATEGORY_BASEMAP
import org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE
import java.io.File
import javax.inject.Inject

class MapsPreferencesFragment : org.espen.collect.android.preferences.screens.BaseProjectPreferencesFragment() {

    private lateinit var basemapSourcePref: ListPreference

    private var referenceLayerPref: org.espen.collect.android.preferences.CaptionedListPreference? = null
    private var autoShowReferenceLayerDialog = false

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (allowClick(javaClass.name)) {
            var dialogFragment: DialogFragment? = null
            if (preference is org.espen.collect.android.preferences.CaptionedListPreference) {
                dialogFragment = org.espen.collect.android.preferences.dialogs.ReferenceLayerPreferenceDialog.newInstance(preference.getKey())
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(
                    parentFragmentManager,
                    org.espen.collect.android.preferences.dialogs.ReferenceLayerPreferenceDialog::class.java.name
                )
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.maps_preferences, rootKey)
        initBasemapSourcePref()
        initReferenceLayerPref()
        if (autoShowReferenceLayerDialog) {
            populateReferenceLayerPref(requireContext(), referenceLayerRepository, referenceLayerPref!!)
            /** Opens the dialog programmatically, rather than by a click from the user.  */
            onDisplayPreferenceDialog(
                preferenceManager.findPreference("reference_layer")!!
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        org.espen.collect.android.injection.DaggerUtils.getComponent(context).inject(this)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (referenceLayerPref != null) {
            populateReferenceLayerPref(requireContext(), referenceLayerRepository, referenceLayerPref!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        referenceLayerPref = null
    }

    /**
     * Creates the Basemap Source preference widget (but doesn't add it to
     * the screen; onBasemapSourceChanged will do that part).
     */
    private fun initBasemapSourcePref() {
        basemapSourcePref = PrefUtils.createListPref(
            requireContext(),
            KEY_BASEMAP_SOURCE,
            getString(org.odk.collect.strings.R.string.basemap_source),
            org.espen.collect.android.geo.MapConfiguratorProvider.getLabelIds(),
            org.espen.collect.android.geo.MapConfiguratorProvider.getIds(),
            settingsProvider.getUnprotectedSettings()
        )

        basemapSourcePref.setIconSpaceReserved(false)
        onBasemapSourceChanged(org.espen.collect.android.geo.MapConfiguratorProvider.getConfigurator())
        basemapSourcePref.setOnPreferenceChangeListener { _: Preference?, value: Any ->
            val cftor = org.espen.collect.android.geo.MapConfiguratorProvider.getConfigurator(value.toString())
            if (!cftor.isAvailable(context)) {
                cftor.showUnavailableMessage(context)
                false
            } else {
                onBasemapSourceChanged(cftor)
                true
            }
        }
    }

    /** Updates the rest of the preference UI when the Basemap Source is changed.  */
    private fun onBasemapSourceChanged(cftor: MapConfigurator) {
        // Set up the preferences in the "Basemap" section.
        val baseCategory = findPreference<PreferenceCategory>(CATEGORY_BASEMAP)
        baseCategory!!.removeAll()
        baseCategory.addPreference(basemapSourcePref)
        for (pref in cftor.createPrefs(context, settingsProvider.getUnprotectedSettings())) {
            pref.isIconSpaceReserved = false
            baseCategory.addPreference(pref)
        }

        // Clear the reference layer if it isn't supported by the new basemap.
        if (referenceLayerPref != null) {
            val path = referenceLayerPref!!.value
            if (path != null && !cftor.supportsLayer(File(path))) {
                referenceLayerPref!!.value = null
                updateReferenceLayerSummary(null)
            }
        }
    }

    /** Sets up listeners for the Reference Layer preference widget.  */
    private fun initReferenceLayerPref() {
        referenceLayerPref = findPreference("reference_layer")
        referenceLayerPref!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                populateReferenceLayerPref(requireContext(), referenceLayerRepository, referenceLayerPref!!)
                false
            }
        if (referenceLayerPref!!.value == null || referenceLayerRepository.get(
                referenceLayerPref!!.value
            ) != null
        ) {
            updateReferenceLayerSummary(referenceLayerPref!!.value)
        } else {
            referenceLayerPref!!.value = null
            updateReferenceLayerSummary(null)
        }
        referenceLayerPref!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                updateReferenceLayerSummary(newValue)
                val dialogFragment = parentFragmentManager.findFragmentByTag(
                    org.espen.collect.android.preferences.dialogs.ReferenceLayerPreferenceDialog::class.java.name
                ) as DialogFragment?
                dialogFragment?.dismiss()
                true
            }
    }

    /** Sets the summary text for the reference layer to show the selected file.  */
    private fun updateReferenceLayerSummary(value: Any?) {
        if (referenceLayerPref != null) {
            val summary: String = if (value == null) {
                getString(org.odk.collect.strings.R.string.none)
            } else {
                val referenceLayer = referenceLayerRepository.get(value.toString())

                if (referenceLayer != null) {
                    val path = referenceLayer.file.absolutePath
                    val cftor = org.espen.collect.android.geo.MapConfiguratorProvider.getConfigurator()
                    cftor.getDisplayName(File(path))
                } else {
                    getString(org.odk.collect.strings.R.string.none)
                }
            }

            referenceLayerPref!!.summary = summary
        }
    }

    companion object {

        /** Pops up the preference dialog that lets the user choose a reference layer.  */
        @JvmStatic
        fun showReferenceLayerDialog(activity: FragmentActivity) {
            // Unfortunately, the Preference class is designed so that it is impossible
            // to just open a preference dialog without building a PreferenceFragment
            // and attaching it to an activity.  So, we instantiate a MapsPreference
            // fragment that is configured to immediately open the dialog when it's
            // attached, then instantiate it and attach it.
            val prefs = MapsPreferencesFragment()
            prefs.autoShowReferenceLayerDialog = true // makes dialog open immediately
            activity.supportFragmentManager
                .beginTransaction()
                .add(prefs, null)
                .commit()
        }
    }
}
