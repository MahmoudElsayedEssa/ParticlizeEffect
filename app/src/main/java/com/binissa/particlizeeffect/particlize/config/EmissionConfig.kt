package com.binissa.particlizeeffect.particlize.config

import com.binissa.particlizeeffect.particlize.emission.EmissionPattern
import com.binissa.particlizeeffect.particlize.emission.EmissionType

/**
 * Configuration for how particles are emitted
 */
data class EmissionConfig(
    val type: EmissionType = EmissionType.PATTERNED,
    val pattern: EmissionPattern = EmissionPattern.LEFT_TO_RIGHT,
    val particleCount: Int = 10000,
    val durationFactor: Float = 1.0f
) {
    companion object {
        val Default = EmissionConfig()
        
        // Preset configurations
        val InstantExplosion = EmissionConfig(
            type = EmissionType.INSTANT,
            particleCount = 8000
        )
        
        val LeftToRightWave = EmissionConfig(
            type = EmissionType.PATTERNED,
            pattern = EmissionPattern.LEFT_TO_RIGHT,
            durationFactor = 1.2f,
            particleCount = 12000
        )
        
        val CenterOutBurst = EmissionConfig(
            type = EmissionType.PATTERNED,
            pattern = EmissionPattern.CENTER_OUT,
            durationFactor = 0.8f,
            particleCount = 10000
        )
    }
}