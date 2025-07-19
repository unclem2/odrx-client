package com.rian.osu.mods

import com.rian.osu.beatmap.sections.BeatmapDifficulty

/**
 * Represents the Relax mod.
 */
class ModRelax : Mod() {
    override val name = "Relax"
    override val acronym = "RX"
    override val description = "You don't need to tap. Give your tapping fingers a break from the heat of things."
    override val type = ModType.Automation
    override val isRanked = true
    override val incompatibleMods = super.incompatibleMods + arrayOf(
        ModAutoplay::class, ModAutopilot::class
    )

    override fun calculateScoreMultiplier(difficulty: BeatmapDifficulty) = 1f
    override fun deepCopy() = ModRelax()
}