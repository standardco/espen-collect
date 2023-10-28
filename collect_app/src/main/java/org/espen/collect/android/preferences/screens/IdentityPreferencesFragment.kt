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
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import org.odk.collect.analytics.Analytics
import org.espen.collect.android.R
import org.espen.collect.android.injection.DaggerUtils
import org.espen.collect.android.preferences.utilities.PreferencesUtils
import org.espen.collect.android.version.VersionInformation
import org.espen.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.settings.keys.ProjectKeys
import javax.inject.Inject

class IdentityPreferencesFragment : org.espen.collect.android.preferences.screens.BaseProjectPreferencesFragment() {
    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var versionInformation: org.espen.collect.android.version.VersionInformation

    override fun onAttach(context: Context) {
        super.onAttach(context)
        org.espen.collect.android.injection.DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.identity_preferences, rootKey)
        findPreference<Preference>("form_metadata")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener listener@{
                if (MultiClickGuard.allowClick(javaClass.name)) {
                    displayPreferences(org.espen.collect.android.preferences.screens.FormMetadataPreferencesFragment())
                    return@listener true
                }
                false
            }
        initAnalyticsPref()
    }

    private fun initAnalyticsPref() {
        val analyticsPreference = findPreference<Preference>(ProjectKeys.KEY_ANALYTICS) as CheckBoxPreference?
        if (analyticsPreference != null) {
            if (versionInformation.isBeta) {
                org.espen.collect.android.preferences.utilities.PreferencesUtils.displayDisabled(analyticsPreference, true)
                analyticsPreference.summary =
                    analyticsPreference.summary.toString() + " Usage data collection cannot be disabled in beta versions of EspenCollect."
            } else {
                analyticsPreference.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        analytics.setAnalyticsCollectionEnabled(analyticsPreference.isChecked)
                        true
                    }
            }
        }
    }
}
