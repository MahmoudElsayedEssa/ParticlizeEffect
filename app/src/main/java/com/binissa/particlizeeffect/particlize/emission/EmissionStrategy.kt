package com.binissa.particlizeeffect.particlize.emission

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PixelMap
import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.particle.Particle
import com.binissa.particlizeeffect.particlize.particle.ParticleEffect

/**
 * Interface for particle emission strategies
 */
interface EmissionStrategy {
    /**
     * Generates particles based on a source bitmap
     */
    fun generateParticles(
        bitmap: ImageBitmap,
        pixelMap: PixelMap,
        particleConfig: ParticleConfig,
        emissionConfig: EmissionConfig,
        effect: ParticleEffect
    ): List<Particle>
}

/**
 * Enum defining the emission patterns
 */
enum class EmissionPattern {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP,
    CENTER_OUT,
    OUTSIDE_IN
}

/**
 * Enum defining the emission types
 */
enum class EmissionType {
    INSTANT,
    PATTERNED
}