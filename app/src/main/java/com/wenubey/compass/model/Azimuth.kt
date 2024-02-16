package com.wenubey.compass.model

import kotlin.math.roundToInt

/**
 * Azimuth represents an angle in degrees, measured clockwise from the north direction.
 * It provides utility functions to normalize the angle, calculate rounded degrees,
 * determine cardinal directions, and perform arithmetic operations.
 *
 * @param _degrees Initial angle in degrees.
 * @throws IllegalArgumentException if the provided degrees is not finite.
 */
class Azimuth(_degrees: Float) {

    /**
     * The normalized angle in degrees, between 0 (inclusive) and 360 (exclusive).
     */
    val degrees = normalizeAngle(_degrees)

    /**
     * The rounded angle in degrees, to the nearest integer.
     */
    val roundedDegrees = normalizeAngle(_degrees.roundToInt().toFloat()).toInt()

    /**
     * The cardinal direction associated with the azimuth angle.
     * It is determined based on predefined ranges of angles.
     */
    val cardinalDirection: CardinalDirection = when(degrees) {
        in 22.5f until 67.5f -> CardinalDirection.NORTHEAST
        in 67.5f until 112.5f -> CardinalDirection.EAST
        in 112.5f until 157.5f -> CardinalDirection.SOUTHEAST
        in 157.5f until 202.5f -> CardinalDirection.SOUTH
        in 202.5f until 247.5f -> CardinalDirection.SOUTHWEST
        in 247.5f until 292.5f -> CardinalDirection.WEST
        in 292.5f until 337.5f -> CardinalDirection.NORTHWEST
        else -> CardinalDirection.NORTH
    }

    /**
     * Normalizes the angle to be within the range [0, 360).
     *
     * @param angleInDegrees Angle in degrees to normalize.
     * @return Normalized angle in degrees.
     */
    private fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360f) % 360f
    }

    /**
     * Checks if two azimuth angles are equal.
     *
     * @param other The other object to compare.
     * @return True if the angles are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Azimuth

        if (degrees != other.degrees) return false

        return true
    }

    /**
     * Generates a hash code for the azimuth angle.
     *
     * @return Hash code for the azimuth angle.
     */
    override fun hashCode(): Int {
        return degrees.hashCode()
    }

    /**
     * Provides a string representation of the azimuth angle.
     *
     * @return String representation of the azimuth angle.
     */
    override fun toString(): String {
        return "Azimuth(degrees:$degrees)"
    }

    /**
     * Adds the specified angle to the current azimuth angle.
     *
     * @param degrees Angle in degrees to add.
     * @return New Azimuth object representing the result of addition.
     */
    operator fun plus(degrees: Float) = Azimuth(this.degrees + degrees)

    /**
     * Subtracts the specified angle from the current azimuth angle.
     *
     * @param degrees Angle in degrees to subtract.
     * @return New Azimuth object representing the result of subtraction.
     */
    operator fun minus(degrees: Float) = Azimuth(this.degrees - degrees)

    /**
     * Compares this azimuth angle to another azimuth angle.
     *
     * @param azimuth The other azimuth angle to compare.
     * @return -1 if this angle is less than the other, 0 if they are equal,
     * or 1 if this angle is greater than the other.
     */
    operator fun compareTo(azimuth: Azimuth) = this.degrees.compareTo(azimuth.degrees)
}

/**
 * Represents a semi-closed range of float values from [fromInclusive] to [toExclusive).
 */
private data class SemiClosedFloatRange(val fromInclusive: Float, val toExclusive: Float)

/**
 * Checks if the specified value is within the semi-closed float range.
 *
 * @param value The value to check.
 * @return True if the value is within the range, false otherwise.
 */
private operator fun SemiClosedFloatRange.contains(value: Float) = fromInclusive <= value && value < toExclusive

/**
 * Creates a semi-closed float range from the current value to the specified end value.
 *
 * @param to The end value of the range (exclusive).
 * @return Semi-closed float range from the current value to [to].
 */
private infix fun Float.until(to: Float) = SemiClosedFloatRange(this, to)
