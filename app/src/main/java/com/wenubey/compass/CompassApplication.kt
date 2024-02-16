package com.wenubey.compass

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.wenubey.compass.preference.PreferencesMigrations

class CompassApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        val preferencesMigrations = PreferencesMigrations(applicationContext)
        preferencesMigrations.migrateV5()
    }
}