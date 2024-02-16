package com.wenubey.compass.util

import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import com.wenubey.compass.model.Azimuth
import com.wenubey.compass.model.DisplayRotation
import com.wenubey.compass.model.RotationVector
import kotlin.math.roundToInt

private const val AZIMUTH = 0
private const val AXIS_SIZE = 3
private const val ROTATION_MATRIX_SIZE = 9

object MathUtils {

    @JvmStatic
    fun calculateAzimuth(
        rotationVector: RotationVector,
        displayRotation: DisplayRotation
    ): Azimuth {
        val rotationMatrix = getRotationMatrix(rotationVector)
        val mappedRotationMatrix = mapRotationMatrix(rotationMatrix, displayRotation)
        val orientationInRadians = SensorManager.getOrientation(mappedRotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInRadians = orientationInRadians[AZIMUTH]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
        return Azimuth(azimuthInDegrees)
    }

    private fun getRotationMatrix(rotationVector: RotationVector): FloatArray {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector.toArray())
        return rotationMatrix
    }

    private fun mapRotationMatrix(rotationMatrix: FloatArray, displayRotation: DisplayRotation): FloatArray {
        return when(displayRotation) {
            DisplayRotation.ROTATION_0 -> mapRotationMatrix(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y)
            DisplayRotation.ROTATION_90 -> mapRotationMatrix(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
            DisplayRotation.ROTATION_180 -> mapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
            DisplayRotation.ROTATION_270 -> mapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
        }
    }

    private fun mapRotationMatrix(rotationMatrix: FloatArray, newX: Int, newY: Int): FloatArray {
        val mappedRotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, mappedRotationMatrix)
        return mappedRotationMatrix
    }

    @JvmStatic
    fun getMagneticDeclination(location: Location): Float {
        val latitude = location.latitude.toFloat()
        val longitude = location.longitude.toFloat()
        val altitude = location.altitude.toFloat()
        val time = location.time
        val geomagneticField = GeomagneticField(latitude, longitude, altitude, time)
        return geomagneticField.declination
    }

    fun getClosestNumberFromInterval(number: Float, interval: Float): Float =
        (number / interval).roundToInt() * interval

    fun isAzimuthBetweenTwoPoints(azimuth: Azimuth, pointA: Azimuth, pointB: Azimuth): Boolean  {
        val aToB = (pointB.degrees - pointA.degrees + 360f) % 360f
        val aToAzimuth = (azimuth.degrees - pointA.degrees + 360f) % 360f
        return aToB <= 180f != aToAzimuth > aToB
    }
}