package com.wenubey.compass.preference

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.wenubey.compass.model.AppNightMode

private const val TAG = "PreferencesStore"

/**
 * Manages the storage and retrieval of application preferences.
 * This class handles the storage and observation of various preferences used within the application.
 * It provides methods to update preferences and observes changes to persist them.
 *
 * @param context The context of the application.
 * @param lifecycle The lifecycle of the owner component, typically an Activity or Fragment.
 */
class PreferencesStore(context: Context, lifecycle: Lifecycle) {

    // MutableLiveData objects to hold preference values and notify observers of changes.
    val trueNorth = MutableLiveData<Boolean>()
    val hapticFeedback = MutableLiveData<Boolean>()
    val screenOrientationLocked = MutableLiveData<Boolean>()
    val nightMode = MutableLiveData<AppNightMode>()
    val accessLocationPermissionRequested = MutableLiveData<Boolean>()

    // Lifecycle observer to manage preference observation and cleanup.
    private val preferencesStoreLifecycleObserver = PreferencesStoreLifecycleObserver()

    // SharedPreferences change listener to detect preference changes.
    private val sharedPreferencesChangeListener = SharedPreferencesChangeListener()

    // Observer functions to persist preference changes.
    private val trueNorthObserver = getTrueNorthObserver()
    private val hapticFeedbackObserver = getHapticFeedbackObserver()
    private val screenOrientationLockedObserver = getScreenOrientationLockedObserver()
    private val nightModeObserver = getNightModeObserver()
    private val accessLocationPermissionRequestedObserver = getAccessLocationPermissionRequestedObserver()

    // Shared preferences instance for accessing preferences.
    private val sharedPreferences: SharedPreferences

    init {
        // Initialize the shared preferences using the default shared preferences of the provided context.
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Update initial preference values.
        updateTrueNorth()
        updateHapticFeedback()
        updateScreenOrientationLocked()
        updateNightMode()
        updateAccessLocationPermissionRequested()

        // Add the lifecycle observer to handle preference observation and cleanup.
        lifecycle.addObserver(preferencesStoreLifecycleObserver)
    }

    /**
     * Lifecycle observer for managing preference observation and cleanup.
     */
    private inner class PreferencesStoreLifecycleObserver : DefaultLifecycleObserver {

        override fun onCreate(owner: LifecycleOwner) {
            // Start observing preference changes when the owner's onCreate() method is called.
            trueNorth.observeForever(trueNorthObserver)
            hapticFeedback.observeForever(hapticFeedbackObserver)
            screenOrientationLocked.observeForever(screenOrientationLockedObserver)
            nightMode.observeForever(nightModeObserver)
            accessLocationPermissionRequested.observeForever(accessLocationPermissionRequestedObserver)

            // Register the shared preferences change listener.
            sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesChangeListener)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            // Stop observing preference changes when the owner's onDestroy() method is called.
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesChangeListener)

            trueNorth.removeObserver(trueNorthObserver)
            hapticFeedback.removeObserver(hapticFeedbackObserver)
            screenOrientationLocked.removeObserver(screenOrientationLockedObserver)
            nightMode.removeObserver(nightModeObserver)
            accessLocationPermissionRequested.removeObserver(accessLocationPermissionRequestedObserver)
        }
    }

    /**
     * SharedPreferences change listener to detect preference changes and update the corresponding LiveData objects.
     */
    private inner class SharedPreferencesChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            // Handle preference changes based on the key.
            when (key) {
                PreferencesConstants.TRUE_NORTH -> updateTrueNorth()
                PreferencesConstants.HAPTIC_FEEDBACK -> updateHapticFeedback()
                PreferencesConstants.SCREEN_ORIENTATION_LOCKED -> updateScreenOrientationLocked()
                PreferencesConstants.NIGHT_NODE -> updateNightMode()
                PreferencesConstants.ACCESS_LOCATION_PERMISSION_REQUESTED -> updateAccessLocationPermissionRequested()
            }
        }
    }

    // Methods to update preference LiveData objects and persist changes to SharedPreferences.

    /**
     * Updates the [trueNorth] preference.
     */
    private fun updateTrueNorth() {
        val storedValue = sharedPreferences.getBoolean(PreferencesConstants.TRUE_NORTH, false)
        if (trueNorth.value != storedValue) {
            trueNorth.value = storedValue
        }
    }

    /**
     * Updates the [hapticFeedback] preference.
     */
    private fun updateHapticFeedback() {
        val storedValue = sharedPreferences.getBoolean(PreferencesConstants.HAPTIC_FEEDBACK, true)
        if (hapticFeedback.value != storedValue) {
            hapticFeedback.value = storedValue
        }
    }

    /**
     * Updates the [screenOrientationLocked] preference.
     */
    private fun updateScreenOrientationLocked() {
        val storedValue = sharedPreferences.getBoolean(PreferencesConstants.SCREEN_ORIENTATION_LOCKED, false)
        if (screenOrientationLocked.value != storedValue) {
            screenOrientationLocked.value = storedValue
        }
    }

    /**
     * Updates the [nightMode] preference.
     */
    private fun updateNightMode() {
        val storedValue = sharedPreferences.getString(PreferencesConstants.NIGHT_NODE, AppNightMode.FOLLOW_SYSTEM.preferenceValue)
            ?.let { AppNightMode.mapToPreferenceValue(it) }
            ?: run { AppNightMode.FOLLOW_SYSTEM }

        if (nightMode.value != storedValue) {
            nightMode.value = storedValue
        }
    }

    /**
     * Updates the [accessLocationPermissionRequested] preference.
     */
    private fun updateAccessLocationPermissionRequested() {
        val storedValue = sharedPreferences.getBoolean(PreferencesConstants.ACCESS_LOCATION_PERMISSION_REQUESTED, false)
        if (accessLocationPermissionRequested.value != storedValue) {
            accessLocationPermissionRequested.value = storedValue
        }
    }

    // Observer functions to persist preference changes when LiveData objects are updated.

    /**
     * Observer function to persist changes to the [trueNorth] preference.
     */
    private fun getTrueNorthObserver(): (t: Boolean) -> Unit = {
        val edit = sharedPreferences.edit()
        edit.putBoolean(PreferencesConstants.TRUE_NORTH, it)
        edit.apply()
        Log.d(TAG, "TrueNorth: $it")
    }

    /**
     * Observer function to persist changes to the [hapticFeedback] preference.
     */
    private fun getHapticFeedbackObserver(): (t: Boolean) -> Unit = {
        val edit = sharedPreferences.edit()
        edit.putBoolean(PreferencesConstants.HAPTIC_FEEDBACK, it)
        edit.apply()
        Log.d(TAG, "Persisted hapticFeedback: $it")
    }

    /**
     * Observer function to persist changes to the [screenOrientationLocked] preference.
     */
    private fun getScreenOrientationLockedObserver(): (t: Boolean) -> Unit = {
        val edit = sharedPreferences.edit()
        edit.putBoolean(PreferencesConstants.SCREEN_ORIENTATION_LOCKED, it)
        edit.apply()
        Log.d(TAG, "Persisted screenOrientationLocked: $it")
    }

    /**
     * Observer function to persist changes to the [nightMode] preference.
     */
    private fun getNightModeObserver(): (t: AppNightMode) -> Unit = {
        val edit = sharedPreferences.edit()
        edit.putString(PreferencesConstants.NIGHT_NODE, it.preferenceValue)
        edit.apply()
        Log.d(TAG, "Persisted nightMode: $it")
    }

    /**
     * Observer function to persist changes to the [accessLocationPermissionRequested] preference.
     */
    private fun getAccessLocationPermissionRequestedObserver(): (t: Boolean) -> Unit = {
        val edit = sharedPreferences.edit()
        edit.putBoolean(PreferencesConstants.ACCESS_LOCATION_PERMISSION_REQUESTED, it)
        edit.apply()
        Log.d(TAG, "Persisted accessLocationPermissionRequested: $it")
    }
}
