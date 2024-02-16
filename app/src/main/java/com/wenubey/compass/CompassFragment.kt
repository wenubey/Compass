package com.wenubey.compass

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.Display
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wenubey.compass.databinding.FragmentCompassBinding
import com.wenubey.compass.databinding.SensorAlertDialogViewBinding
import com.wenubey.compass.model.AppError
import com.wenubey.compass.model.Azimuth
import com.wenubey.compass.model.DisplayRotation
import com.wenubey.compass.model.LocationStatus
import com.wenubey.compass.model.RotationVector
import com.wenubey.compass.model.SensorAccuracy
import com.wenubey.compass.preference.PreferencesStore
import com.wenubey.compass.util.MathUtils
import com.wenubey.compass.view.CompassViewModel
import java.util.concurrent.Executor

const val OPTION_INSTRUMENTED_TEST = "INSTRUMENTED_TEST"

private const val TAG = "Compass Fragment"

class CompassFragment : Fragment() {

    private val compassViewModel: CompassViewModel by viewModels()
    private val compassMenuProvider = CompassMenuProvider()
    private val compassSensorEventListener = CompassSensorEventListener()

    private lateinit var binding: FragmentCompassBinding
    private lateinit var preferencesStore: PreferencesStore

    private var sensorManager: SensorManager? = null
    private var locationManager: LocationManager? = null
    private var locationRequestCancellationSignal: CancellationSignal? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBinding()
        initPreferencesStore()
        setupSystemServices()
        setupMenu()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = compassViewModel
        binding.locationReloadButton.setOnClickListener { requestLocation() }
    }

    private fun initPreferencesStore() {
        preferencesStore = PreferencesStore(requireContext(), lifecycle)
        preferencesStore.trueNorth.observe(viewLifecycleOwner) {
            compassViewModel.trueNorth.value = it
        }
        preferencesStore.hapticFeedback.observe(viewLifecycleOwner) {
            compassViewModel.hapticFeedback.value = it
        }
    }

    private fun setupSystemServices() {
        sensorManager = ActivityCompat.getSystemService(requireContext(), SensorManager::class.java)
        locationManager =
            ActivityCompat.getSystemService(requireContext(), LocationManager::class.java)
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(
            compassMenuProvider,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    override fun onResume() {
        super.onResume()

        if (isInstrumentedTest()) {
            Log.i(TAG, "Skipping start of system functionalities")
        } else {
            startSystemServiceFunctionalities()
        }

        Log.i(TAG, "Started compass")
    }

    private fun isInstrumentedTest() =
        requireActivity().intent.extras?.getBoolean(OPTION_INSTRUMENTED_TEST) ?: false

    private fun startSystemServiceFunctionalities() {
        registerSensorListener()

        if (compassViewModel.trueNorth.value == true && compassViewModel.location.value == null) {
            requestLocation()
        }
    }

    private fun registerSensorListener() {
        sensorManager
            ?.also(::registerSensorListener)
            ?: run {
                Log.w(TAG, "SensorManager not present")
                showErrorDialog(AppError.SENSOR_MANAGER_NOT_PRESENT)
            }
    }

    private fun registerSensorListener(sensorManager: SensorManager) {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?.also { rotationVectorSensor ->
                registerRotationVectorSensorListener(
                    sensorManager,
                    rotationVectorSensor
                )
            }
            ?: run {
                Log.w(TAG, "Rotation vector sensor not available")
                showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_NOT_AVAILABLE)
            }

        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            ?.also { magneticFieldSensor ->
                registerMagneticFieldSensorListener(
                    sensorManager,
                    magneticFieldSensor
                )
            }
            ?: run {
                Log.w(TAG, "Magnetic field sensor not available")
                showErrorDialog(AppError.MAGNETIC_FIELD_SENSOR_NOT_AVAILABLE)
            }
    }

    private fun registerRotationVectorSensorListener(
        sensorManager: SensorManager,
        rotationVectorSensor: Sensor
    ) {
        val success = sensorManager.registerListener(
            compassSensorEventListener,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        if (success) {
            Log.d(TAG, "Registered listener for rotation vector sensor")
        } else {
            Log.w(TAG, "Couldn't enable rotation vector sensor")
            showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_FAILED)
        }
    }

    private fun registerMagneticFieldSensorListener(
        sensorManager: SensorManager,
        magneticFieldSensor: Sensor
    ) {
        val success = sensorManager.registerListener(
            compassSensorEventListener,
            magneticFieldSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        if (success) {
            Log.d(TAG, "Registered listener for magnetic field sensor")
        } else {
            Log.w(TAG, "Couldn't enable magnetic field sensor")
            showErrorDialog(AppError.MAGNETIC_FIELD_SENSOR_FAILED)
        }
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_COARSE_LOCATION
            ) == PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            registerLocationListener()
        } else {
            compassViewModel.locationStatus.value = LocationStatus.PERMISSION_DENIED
        }
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun registerLocationListener() {
        locationManager
            ?.also(::registerLocationListener)
            ?: run {
                Log.w(TAG, "LocationManager not present")
                setLocation(null)
                showErrorDialog(AppError.LOCATION_MANAGER_NOT_PRESENT)
            }
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun registerLocationListener(locationManager: LocationManager) {
        if (LocationManagerCompat.isLocationEnabled(locationManager)) {
            requestLocation(locationManager)
        } else {
            Log.w(TAG, "Location is disabled")
            setLocation(null)
            showErrorDialog(AppError.LOCATION_DISABLED)
        }
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun requestLocation(locationManager: LocationManager) {
        getBestLocationProvider(locationManager)
            ?.also { provider -> requestLocation(locationManager, provider) }
            ?: run {
                Log.w(TAG, "No LocationProvider available")
                setLocation(null)
                showErrorDialog(AppError.NO_LOCATION_PROVIDER_AVAILABLE)
            }
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun requestLocation(locationManager: LocationManager, provider: String) {
        Log.i(TAG, "Request location from provider '$provider'")

        compassViewModel.locationStatus.value = LocationStatus.LOADING

        locationRequestCancellationSignal?.cancel()
        locationRequestCancellationSignal = CancellationSignal()

        LocationManagerCompat.getCurrentLocation(
            locationManager,
            provider,
            locationRequestCancellationSignal,
            getExecutor(),
            ::setLocation
        )
    }

    private fun getExecutor(): Executor = ContextCompat.getMainExecutor(requireContext())

    private fun getBestLocationProvider(locationManager: LocationManager): String? {
        val availableProviders = locationManager.getProviders(true)

        for (preferredProvider in getPreferredProviders()) {
            if (availableProviders.contains(preferredProvider)) {
                return preferredProvider
            }
        }

        return null
    }

    private fun getPreferredProviders(): List<String> {
        val preferredProviders = mutableListOf<String>()

        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            preferredProviders.add(LocationManager.FUSED_PROVIDER)
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_COARSE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            preferredProviders.add(LocationManager.GPS_PROVIDER)
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            preferredProviders.add(LocationManager.NETWORK_PROVIDER)
        }

        return preferredProviders
    }

    private fun showErrorDialog(appError: AppError) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.error)
            .setIcon(R.drawable.ic_error)
            .setMessage(
                getString(
                    R.string.error_message,
                    getString(appError.messageId),
                    appError.name
                )
            )
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(compassSensorEventListener)
        locationRequestCancellationSignal?.cancel()
        Log.i(TAG, "Stopped compass")
    }

    private inner class CompassMenuProvider : MenuProvider {

        private var optionsMenu: Menu? = null

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_metronome, menu)
            optionsMenu = menu
            compassViewModel.sensorAccuracy.observe(viewLifecycleOwner) { updateSensorStatusIcon(it) }
            preferencesStore.screenOrientationLocked.observe(viewLifecycleOwner) {
                updateScreenRotationIcon(
                    it
                )
            }
        }

        private fun updateSensorStatusIcon(sensorAccuracy: SensorAccuracy) {
            val menuItem = optionsMenu?.findItem(R.id.action_sensor_status)
            menuItem?.setIcon(sensorAccuracy.iconResId)

            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                sensorAccuracy.iconTintAttrResId
                    .let { MaterialColors.getColor(requireContext(), it, this::class.simpleName) }
                    .let { ColorStateList.valueOf(it) }
                    .also { menuItem?.iconTintList = it }
            }
        }

        private fun updateScreenRotationIcon(screenOrientationLocked: Boolean) {
            optionsMenu
                ?.findItem(R.id.action_screen_rotation)
                ?.setIcon(getScreenRotationIcon(screenOrientationLocked))
        }

        @DrawableRes
        private fun getScreenRotationIcon(screenOrientationLocked: Boolean): Int =
            if (screenOrientationLocked) R.drawable.ic_screen_rotation_lock else R.drawable.ic_screen_rotation

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_sensor_status -> {
                    showSensorStatusPopup()
                    true
                }

                R.id.action_screen_rotation -> {
                    toggleRotationScreenLocked()
                    true
                }

                R.id.action_settings -> {
                    showSettings()
                    true
                }

                else -> false
            }
        }

        private fun showSensorStatusPopup() {
            val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
            val dialogContextInflater = LayoutInflater.from(alertDialogBuilder.context)

            val dialogBinding = SensorAlertDialogViewBinding.inflate(dialogContextInflater, null, false)
            dialogBinding.model = compassViewModel
            dialogBinding.lifecycleOwner = viewLifecycleOwner

            alertDialogBuilder
                .setTitle(R.string.sensor_status)
                .setView(dialogBinding.root)
                .setPositiveButton(R.string.ok) { dialog ,_ -> dialog.dismiss() }
                .show()

        }

        private fun toggleRotationScreenLocked() {
            preferencesStore.screenOrientationLocked.value?.let {
                preferencesStore.screenOrientationLocked.value = it.not()
            }
        }

        private fun showSettings() {
            findNavController().navigate(R.id.action_CompassFragment_to_SettingsFragment)
        }
    }

    private inner class CompassSensorEventListener: SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            when(sensor.type) {
                Sensor.TYPE_MAGNETIC_FIELD -> setSensorAccuracy(accuracy)
                Sensor.TYPE_ROTATION_VECTOR -> Log.v(TAG, "Received rotation vector sensor accuracy '$accuracy'")
                else -> Log.w(TAG, "Unexpected accuracy changed event of '${sensor.type}'")
            }
        }

        private fun setSensorAccuracy(accuracy: Int) {
            val sensorAccuracy = adaptSensorAccuracy(accuracy)
            setSensorAccuracy(sensorAccuracy)
        }

        private fun adaptSensorAccuracy(accuracy: Int): SensorAccuracy {
            return when(accuracy) {
                SensorManager.SENSOR_STATUS_NO_CONTACT -> SensorAccuracy.NO_CONTACT
                SensorManager.SENSOR_STATUS_UNRELIABLE -> SensorAccuracy.UNRELIABLE
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
                else -> {
                    Log.w(TAG, "Encountered sensor accuracy value '$accuracy'")
                    SensorAccuracy.NO_CONTACT
                }
            }
        }

        override fun onSensorChanged(event: SensorEvent) {
            when(event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> updateCompass(event)
                Sensor.TYPE_MAGNETIC_FIELD -> Log.v(TAG, "Received magnetic field sensor event ${event.values}")
                else -> Log.w(TAG, "Unexpected sensor changed event of '${event.sensor.type}'")
            }
        }

        private fun updateCompass(event: SensorEvent) {
            val rotationVector = RotationVector(event.values[0], event.values[1], event.values[2])
            val displayRotation = getDisplayRotation()
            val magneticAzimuth = MathUtils.calculateAzimuth(rotationVector, displayRotation)

            if (compassViewModel.trueNorth.value == true) {
                val magneticDeclination = getMagneticDeclination()
                val trueAzimuth = magneticAzimuth.plus(magneticDeclination)
                setAzimuth(magneticAzimuth)
            } else {
                setAzimuth(magneticAzimuth)
            }
        }

        private fun getDisplayRotation(): DisplayRotation {
            return when(getDisplayCompat()?.rotation) {
                Surface.ROTATION_90 -> DisplayRotation.ROTATION_90
                Surface.ROTATION_180 -> DisplayRotation.ROTATION_180
                Surface.ROTATION_270 -> DisplayRotation.ROTATION_270
                else -> DisplayRotation.ROTATION_0
            }
        }

        private fun getDisplayCompat(): Display? {
            return if (VERSION.SDK_INT >= VERSION_CODES.R) {
                requireContext().display
            } else {
                @Suppress("DEPRECATION")
                requireActivity().windowManager.defaultDisplay
            }
        }

        private fun getMagneticDeclination(): Float {
            return compassViewModel.location.value
                ?.let(MathUtils::getMagneticDeclination)
                ?: 0.0f
        }
    }

    private fun setLocation(location: Location?) {
        Log.i(TAG, "Location: $location")
        compassViewModel.location.value = location

        compassViewModel.locationStatus.value = when(location) {
            null -> LocationStatus.NOT_PRESENT
            else -> LocationStatus.PRESENT
        }
    }

    internal fun setSensorAccuracy(sensorAccuracy: SensorAccuracy) {
        Log.i(TAG, "Sensor accuracy $sensorAccuracy")
        compassViewModel.sensorAccuracy.value = sensorAccuracy
    }

    internal fun setAzimuth(azimuth: Azimuth) {
        Log.v(TAG, "Azimuth: $azimuth")
        compassViewModel.azimuth.value = azimuth
    }
}