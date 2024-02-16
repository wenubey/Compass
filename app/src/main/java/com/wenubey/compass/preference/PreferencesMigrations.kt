package com.wenubey.compass.preference

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate


private const val NIGHT_MODE = "night_mode"
private const val NO = "no"
private const val YES = "yes"
private const val FOLLOW_SYSTEM = "followSystem"

private const val TAG = "PreferencesMigrations"

private const val MIGRATION_V11 = "migration_v11"


class PreferencesMigrations(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * Migrate night_mode preference from integer to string values
     */
    fun migrateV5() {
        val migrated = sharedPreferences.getBoolean(MIGRATION_V11, false)

        if (!migrated) {
            val nightMode = sharedPreferences.getInt(NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

            val edit = sharedPreferences.edit()
            edit.remove(NIGHT_MODE)

            when(nightMode) {
                AppCompatDelegate.MODE_NIGHT_NO -> edit.putString(NIGHT_MODE, NO)
                AppCompatDelegate.MODE_NIGHT_YES -> edit.putString(NIGHT_MODE, YES)
                else -> edit.putString(NIGHT_MODE, FOLLOW_SYSTEM)
            }

            edit.putBoolean(MIGRATION_V11, true)
            edit.apply()
            Log.i(TAG, "migration completed")
        }
    }

}

