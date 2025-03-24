package com.binissa.particlizeeffect.particlize.config

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing

/**
 * Enhanced configuration for animation timing and behavior
 */
data class AnimationConfig(
    // Base animation properties
    val duration: Int = 1000,
    val easing: Easing = LinearEasing,
    var repeatCount: Int = 0,  // -1 for infinite

    // Visual randomization
    val randomizeTimings: Boolean = true,      // Whether particles have randomized timing offsets
    val timingVariation: Float = 0.2f,         // How much timing can vary (as fraction of duration)
    val randomizeAlpha: Boolean = true,        // Whether particles have randomized alpha
    val alphaVariation: Float = 0.2f,          // How much alpha can vary
    val randomizeScale: Boolean = true,        // Whether particles have randomized scales
    val scaleVariation: Float = 0.15f,         // How much scale can vary

    // Fade effects
    val fadeInDuration: Float = 0.2f,          // How much of the animation is spent fading in (0-1)
    val fadeOutDuration: Float = 0.2f,         // How much of the animation is spent fading out (0-1)

    // Visual effects
    val addOscillation: Boolean = false,       // Whether to add oscillation effects
    val oscillationStrength: Float = 0.2f,     // How strong oscillation effects are
    val oscillationFrequency: Float = 3f       // How fast oscillation happens
) {
    companion object {
        val Default = AnimationConfig()

        // Preset configurations
        val Quick = AnimationConfig(
            duration = 500,
            easing = FastOutSlowInEasing
        )

        val Dramatic = AnimationConfig(
            duration = 1500,
            easing = FastOutSlowInEasing,
            randomizeTimings = true,
            timingVariation = 0.3f,
            randomizeAlpha = true,
            alphaVariation = 0.3f,
            randomizeScale = true,
            scaleVariation = 0.2f,
            fadeInDuration = 0.3f,
            fadeOutDuration = 0.3f
        )

        val Organic = AnimationConfig(
            duration = 2000,
            easing = FastOutSlowInEasing,
            randomizeTimings = true,
            timingVariation = 0.4f,
            randomizeAlpha = true,
            alphaVariation = 0.3f,
            randomizeScale = true,
            scaleVariation = 0.2f,
            addOscillation = true,
            oscillationStrength = 0.3f,
            oscillationFrequency = 2f
        )
    }
}