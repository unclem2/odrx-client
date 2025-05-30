package com.rian.osu.beatmap

/**
 * Represents an osu!droid hit window.
 */
class DroidHitWindow @JvmOverloads constructor(
    /**
     * The overall difficulty of this [DroidHitWindow]. Defaults to 5.
     */
    overallDifficulty: Float? = 5f
) : HitWindow(overallDifficulty) {
    override val greatWindow
        get() = 75 + 5 * (5 - overallDifficulty)

    override val okWindow
        get() = 150 + 10 * (5 - overallDifficulty)

    override val mehWindow
        get() = 250 + 10 * (5 - overallDifficulty)

    companion object {
        /**
         * Calculates the overall difficulty value of a great hit window.
         *
         * @param value The value of the hit window in milliseconds.
         * @return The overall difficulty value.
         */
        @JvmStatic
        fun hitWindow300ToOverallDifficulty(value: Float) = 5 - (value - 75) / 5

        /**
         * Calculates the overall difficulty value of an ok hit window.
         *
         * @param value The value of the hit window in milliseconds.
         * @returns The overall difficulty value.
         */
        @JvmStatic
        fun hitWindow100ToOverallDifficulty(value: Float) = 5 - (value - 150) / 10

        /**
         * Calculates the overall difficulty value of a meh hit window.
         *
         * @param value The value of the hit window in milliseconds.
         * @returns The overall difficulty value.
         */
        @JvmStatic
        fun hitWindow50ToOverallDifficulty(value: Float) = 5 - (value - 250) / 10
    }
}