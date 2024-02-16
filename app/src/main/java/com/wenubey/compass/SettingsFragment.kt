package com.wenubey.compass

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceFragmentCompat
import com.wenubey.compass.preference.PreferencesConstants


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>(PreferencesConstants.VERSION)?.summaryProvider =
            SummaryProvider<Preference> { BuildConfig.VERSION_NAME }
    }

}