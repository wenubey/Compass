package com.wenubey.compass

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wenubey.compass.databinding.ActivityMainBinding
import com.wenubey.compass.model.AppNightMode
import com.wenubey.compass.preference.PreferencesStore

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val accessLocationPermissionRequest = registerAccessLocationPermissionRequest()

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var preferencesStore: PreferencesStore

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        val navController = getNavController()
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        initPreferencesStore()

    }

    private fun initPreferencesStore() {
        preferencesStore = PreferencesStore(this, lifecycle)
        preferencesStore.nightMode.observe(this) { setNightMode(it) }
        preferencesStore.screenOrientationLocked.observe(this) { setScreenRotationMode(it) }
        preferencesStore.trueNorth.observe(this) { setupTrueNorthFeature(it) }
    }

    private fun setNightMode(appNightMode: AppNightMode) {
        Log.d(TAG, "setNightMode: $appNightMode")
        setDefaultNightMode(appNightMode.nightMode)
    }

    private fun setScreenRotationMode(screenOrientationLocked: Boolean) {
        if (screenOrientationLocked) {
            setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
        } else {
            setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        }
    }

    private fun setScreenOrientation(screenOrientation: Int) {
        Log.d(TAG, "setScreenOrientation: $screenOrientation")
        requestedOrientation = screenOrientation
    }

    private fun setupTrueNorthFeature(trueNorth: Boolean?) {
        if (trueNorth == true) {
            handleLocationPermission()
        }
    }

    private fun handleLocationPermission() {
        if (neverRequestedAccessLocationPermission() && accessLocationPermissionDenied()) {
            startAccessLocationPermissionRequestWorkflow()
        }
    }

    private fun neverRequestedAccessLocationPermission() =
        preferencesStore.accessLocationPermissionRequested.value != true

    private fun accessLocationPermissionDenied() =
        ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(
            this,
            ACCESS_FINE_LOCATION
        ) == PERMISSION_DENIED

    private fun startAccessLocationPermissionRequestWorkflow() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                ACCESS_COARSE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)
        ) {
            showRequestNotificationsPermissionRationale()
        } else {
            launchAccessLocationPermissionRequest()
        }
    }

    private fun showRequestNotificationsPermissionRationale() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.access_location_permission_rationale_title)
            .setMessage(R.string.access_location_permission_rationale_message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                launchAccessLocationPermissionRequest()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no_thanks) { dialog, _ ->
                Log.i(TAG, "Continuing without requesting location permission")
                preferencesStore.accessLocationPermissionRequested.value = true
                dialog.dismiss()
            }
            .show()
    }

    private fun launchAccessLocationPermissionRequest() {
        Log.i(TAG, "Requesting location permission")
        accessLocationPermissionRequest.launch(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
    }

    private fun registerAccessLocationPermissionRequest() =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[ACCESS_FINE_LOCATION] ?: false -> {
                    Log.i(TAG, "Permission ACCESS_FINE_LOCATION granted")
                }
                permissions[ACCESS_COARSE_LOCATION] ?: false -> {
                    Log.i(TAG, "Permission ACCESS_COARSE_LOCATION granted")
                }

                else -> {
                    Log.i(TAG, "Location permission denied")
                    preferencesStore.accessLocationPermissionRequested.value = true
                }
            }
        }

    override fun onSupportNavigateUp(): Boolean {
        val navController = getNavController()
        return navController.navigateUp(appBarConfiguration) ||
                super.onSupportNavigateUp()
    }

    private fun getNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        return navHostFragment.navController
    }
}