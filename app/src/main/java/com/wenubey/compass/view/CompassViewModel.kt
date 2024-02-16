package com.wenubey.compass.view

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wenubey.compass.model.Azimuth
import com.wenubey.compass.model.LocationStatus
import com.wenubey.compass.model.SensorAccuracy

class CompassViewModel: ViewModel() {

    val azimuth = MutableLiveData<Azimuth>()
    val sensorAccuracy = MutableLiveData(SensorAccuracy.NO_CONTACT)
    val trueNorth = MutableLiveData(false)
    val hapticFeedback = MutableLiveData(true)
    val location = MutableLiveData<Location>()
    val locationStatus = MutableLiveData(LocationStatus.NOT_PRESENT)
}