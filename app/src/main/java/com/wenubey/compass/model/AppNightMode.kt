package com.wenubey.compass.model

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import com.wenubey.compass.preference.PreferencesConstants

enum class AppNightMode(@NightMode val nightMode: Int, val preferenceValue: String) {
    FOLLOW_SYSTEM(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        PreferencesConstants.NIGHT_MODE_VALUE_FOLLOW_SYSTEM
    ),
    NO(AppCompatDelegate.MODE_NIGHT_NO, PreferencesConstants.NIGHT_MODE_VALUE_NO),
    YES(AppCompatDelegate.MODE_NIGHT_YES, PreferencesConstants.NIGHT_MODE_VALUE_YES);

    companion object {
        fun mapToPreferenceValue(preferenceValue: String): AppNightMode {
            return when (preferenceValue) {
                PreferencesConstants.NIGHT_MODE_VALUE_NO -> NO
                PreferencesConstants.NIGHT_MODE_VALUE_YES -> YES
                else -> FOLLOW_SYSTEM
            }
        }
    }

}