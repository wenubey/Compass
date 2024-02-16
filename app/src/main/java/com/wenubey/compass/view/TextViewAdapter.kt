package com.wenubey.compass.view

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.color.MaterialColors

object TextViewAdapter {

    @BindingAdapter("android:compoundDrawableTint")
    @JvmStatic
    fun setCompoundDrawableTint(textView: TextView, @AttrRes colorAttrResId: Int) {
        val color = MaterialColors.getColor(textView, colorAttrResId)
        val colorStateList = ColorStateList.valueOf(color)
        TextViewCompat.setCompoundDrawableTintList(textView, colorStateList)
    }
}