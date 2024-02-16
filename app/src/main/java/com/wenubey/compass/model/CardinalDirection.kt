package com.wenubey.compass.model

import androidx.annotation.StringRes
import com.wenubey.compass.R

enum class CardinalDirection(@StringRes val resId: Int) {
    NORTH(R.string.cardinal_dir_north),
    NORTHEAST(R.string.cardinal_dir_northeast),
    EAST(R.string.cardinal_dir_east),
    SOUTHEAST(R.string.cardinal_dir_southeast),
    SOUTH(R.string.cardinal_dir_south),
    SOUTHWEST(R.string.cardinal_dir_southwest),
    WEST(R.string.cardinal_dir_west),
    NORTHWEST(R.string.cardinal_dir_northwest)
}