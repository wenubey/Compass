package com.wenubey.compass.model

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.wenubey.compass.R

enum class SensorAccuracy(
    @StringRes val textResId: Int,
    @DrawableRes val iconResId: Int,
    @AttrRes val iconTintAttrResId: Int,
) {
    NO_CONTACT(
        R.string.sensor_accuracy_no_contact,
        R.drawable.ic_sensor_no_contact,
        androidx.appcompat.R.attr.colorError
    ),
    UNRELIABLE(
        R.string.sensor_accuracy_unreliable,
        R.drawable.ic_sensor_unreliable,
        androidx.appcompat.R.attr.colorError
    ),
    LOW(
        R.string.sensor_accuracy_low,
        R.drawable.ic_sensor_low,
        androidx.appcompat.R.attr.colorError
    ),
    MEDIUM(
        R.string.sensor_accuracy_medium,
        R.drawable.ic_sensor_medium,
        androidx.appcompat.R.attr.colorControlNormal
    ),
    HIGH(
        R.string.sensor_accuracy_high,
        R.drawable.ic_sensor_high,
        androidx.appcompat.R.attr.colorControlNormal
    ),
}