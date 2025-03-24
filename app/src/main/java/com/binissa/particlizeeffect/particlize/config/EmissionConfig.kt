package com.binissa.particlizeeffect.particlize.config

import com.binissa.particlizeeffect.particlize.emission.EmissionPattern
import com.binissa.particlizeeffect.particlize.emission.EmissionType

/**
 * Enhanced emission configuration with visual randomization
 */
data class EmissionConfig(
    // Basic emission settings
    val type: EmissionType = EmissionType.INSTANT,
    val pattern: EmissionPattern = EmissionPattern.CENTER_OUT,
    val particleCount: Int = 10000,
    val durationFactor: Float = 1.0f,

    // Visual randomization
    val randomSizes: Boolean = true,            // Randomize particle sizes
    val sizeVariationRange: Float = 0.4f,       // How much sizes can vary (±40%)
    val randomAlphas: Boolean = true,           // Randomize particle alphas
    val alphaVariationRange: Float = 0.4f,      // How much alphas can vary (±40%)
    val randomRotations: Boolean = true,        // Randomize initial rotations
    val randomizeColors: Boolean = false,       // Apply slight color variations
    val randomizeDelays: Boolean = true,        // Add randomness to pattern delays

    // Density settings
    val density: Float = 0.5f,                  // Overall density factor
    val sizePriority: Float = 0.5f              // Balance between count and size
) {
    companion object {
        val Default = EmissionConfig()

        val HighRandomization = EmissionConfig(
            randomSizes = true,
            sizeVariationRange = 0.6f,
            randomAlphas = true,
            alphaVariationRange = 0.6f,
            randomRotations = true,
            randomizeColors = true,
            randomizeDelays = true
        )

        val LowRandomization = EmissionConfig(
            randomSizes = true,
            sizeVariationRange = 0.2f,
            randomAlphas = false,
            randomRotations = true,
            randomizeDelays = false
        )
    }
}