package com.rian.osu.mods

import com.rian.osu.GameMode
import com.rian.osu.beatmap.PreciseDroidHitWindow
import com.rian.osu.beatmap.hitobject.HitObject
import com.rian.osu.beatmap.hitobject.Slider
import com.rian.osu.beatmap.hitobject.Spinner

/**
 * Represents the Precise mod.
 */
class ModPrecise : Mod(), IModApplicableToHitObject {
    override val name = "Precise"
    override val acronym = "PR"
    override val description = "Ultimate rhythm gamer timing."
    override val type = ModType.DifficultyIncrease
    override val isRanked = true
    override val scoreMultiplier = 1.06f

    override fun applyToHitObject(
        mode: GameMode,
        hitObject: HitObject,
        adjustmentMods: Iterable<IModFacilitatesAdjustment>
    ) {
        if (mode == GameMode.Standard || hitObject is Spinner) {
            return
        }

        // For sliders, the hit window is enforced in the head - everything else is an instant hit or miss.
        val obj = if (hitObject is Slider) hitObject.head else hitObject

        obj.hitWindow = PreciseDroidHitWindow(obj.hitWindow?.overallDifficulty)
    }

    override fun deepCopy() = ModPrecise()
}