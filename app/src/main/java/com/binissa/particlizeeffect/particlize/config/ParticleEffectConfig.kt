package com.binissa.particlizeeffect.particlize.config

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Color
import com.binissa.particlizeeffect.particlize.emission.EmissionPattern
import com.binissa.particlizeeffect.particlize.emission.EmissionType
import com.binissa.particlizeeffect.particlize.particle.ParticleShape

/**
 * Unified configuration for the entire particle effect system
 */
class ParticleEffectConfig(
    // Common settings
    var particleSize: Float = 4f,
    var particleShape: ParticleShape = ParticleShape.CIRCLE,
    var particleCount: Int = 10000,
    var duration: Int = 1000,
    var easing: Easing = LinearEasing,

    // Emission settings
    var emissionType: EmissionType = EmissionType.INSTANT,
    var emissionPattern: EmissionPattern = EmissionPattern.CENTER_OUT,
    var emissionDuration: Float = 1.0f,  // As a fraction of total duration

    // Physics settings - common
    var friction: Float = 0.98f,
    var turbulenceStrength: Float = 1.0f,
    var turbulenceScale: Float = 0.1f,

    // Physics settings - disintegration specific
    var disintegrationAngle: Float = 90f,  // Upward by default
    var disintegrationAngleVariation: Float = 360f,  // Full random by default
    var disintegrationForce: Float = 2.0f,
    var disintegrationGravity: Float = 9.8f,

    // Physics settings - assembly specific
    var assemblyApproachSpeed: Float = 5.0f,
    var assemblySpringStiffness: Float = 3.0f,
    var assemblySpringDamping: Float = 0.7f,

    // Visual settings
    var fadeRate: Float = 1.2f,
    var scaleRate: Float = 0.5f,
    var rotationSpeed: Float = 2.0f,

    //****

    // Common settings
    var particleSizeVariation: Float = 0.5f,  // Random size variation factor

    // Color and transparency effects
    var useOriginalColors: Boolean = true,
    var tintColor: Color? = null,
    var tintStrength: Float = 0.3f,  // 0-1, how much to tint original colors
    var randomAlpha: Boolean = false,  // Enable random initial alpha values
    var randomAlphaRange: Pair<Float, Float> = Pair(0.5f, 1.0f),  // Min/max for random alpha
    var alphaVariationSpeed: Float = 0f,  // Fluctuation in alpha over time (0 = none)

    // Scale effects
    var initialScale: Float = 1.0f,
    var finalScale: Float = 0.5f,  // For disintegration (shrink). For assembly: grow from initialScale to 1.0
    var scaleVariation: Boolean = false,  // Enable random scale variation
    var scaleVariationRange: Pair<Float, Float> = Pair(0.8f, 1.2f),  // Min/max factor
    var pulsateScale: Boolean = false,  // Scale fluctuation over time
    var pulsateAmplitude: Float = 0.2f,  // How much to pulsate (0-1)
    var pulsateFrequency: Float = 3.0f,  // How many complete pulses during animation

    // Rotation effects
    var initialRotation: Float = 0f,
    var enableRotation: Boolean = true,
    var randomRotationDirection: Boolean = true,  // Randomly clockwise or counter-clockwise
    var rotationVariation: Float = 0.5f,  // Variation in rotation speed between particles

    // Emission settings


    // Lifetime settings
    var randomizeLifetime: Boolean = false,  // Give particles different lifetimes
    var lifetimeRange: Pair<Float, Float> = Pair(
        0.7f, 1.3f
    )  // Min/max factor of animation duration

) {
    /**
     * Convenient method to set directional disintegration
     */
    fun setDisintegrationDirection(
        angle: Float, variation: Float = 60f, force: Float = 2.0f
    ): ParticleEffectConfig {
        disintegrationAngle = angle
        disintegrationAngleVariation = variation
        disintegrationForce = force
        return this
    }

    /**
     * Set emission pattern
     */
    fun setEmission(type: EmissionType, pattern: EmissionPattern): ParticleEffectConfig {
        emissionType = type
        emissionPattern = pattern
        return this
    }

    /**
     * Set turbulence properties
     */
    fun setTurbulence(strength: Float, scale: Float = 0.1f): ParticleEffectConfig {
        turbulenceStrength = strength
        turbulenceScale = scale
        return this
    }

    /**
     * Set animation timing
     */
    fun setTiming(durationMs: Int, easingFunction: Easing = LinearEasing): ParticleEffectConfig {
        duration = durationMs
        easing = easingFunction
        return this
    }


    /**
     * Configure visual appearance
     */
    fun visualAppearance(
        tint: Color? = null,
        randomAlpha: Boolean = false,
        randomScale: Boolean = false,
        pulsate: Boolean = false
    ): ParticleEffectConfig {
        this.tintColor = tint
        this.useOriginalColors = tint == null
        this.randomAlpha = randomAlpha
        this.scaleVariation = randomScale
        this.pulsateScale = pulsate
        return this
    }

    /**
     * Configure rotation effects
     */
    fun rotation(
        enable: Boolean = true,
        speed: Float = 2.0f,
        variation: Float = 0.5f,
        randomDirection: Boolean = true
    ): ParticleEffectConfig {
        this.enableRotation = enable
        this.rotationSpeed = speed
        this.rotationVariation = variation
        this.randomRotationDirection = randomDirection
        return this
    }

    /**
     * Builder method for a standard disintegration effect
     */
    companion object {
        /**
         * Create a standard explosion configuration
         */
        fun explosion(size: Float = 4f, count: Int = 10000): ParticleEffectConfig {
            return ParticleEffectConfig(
                particleSize = size,
                particleCount = count,
                emissionType = EmissionType.INSTANT,
                disintegrationAngleVariation = 360f,  // Full random
                disintegrationForce = 3.0f,           // Stronger force
                turbulenceStrength = 1.5f,            // More turbulence
                duration = 1500                       // 1.5 seconds
            )
        }

        /**
         * Create a directional burst configuration
         */
        fun directionalBurst(
            angle: Float, variation: Float = 30f, size: Float = 4f, count: Int = 10000
        ): ParticleEffectConfig {
            return ParticleEffectConfig(
                particleSize = size,
                particleCount = count,
                emissionType = EmissionType.INSTANT,
                disintegrationAngle = angle,
                disintegrationAngleVariation = variation,
                disintegrationForce = 4.0f,           // Strong force
                turbulenceStrength = 0.8f,            // Medium turbulence
                duration = 1500                       // 1.5 seconds
            )
        }

        /**
         * Create a smooth assembly configuration
         */
        fun smoothAssembly(
            emissionPattern: EmissionPattern = EmissionPattern.CENTER_OUT,
            size: Float = 4f,
            count: Int = 10000
        ): ParticleEffectConfig {
            return ParticleEffectConfig(
                particleSize = size,
                particleCount = count,
                emissionType = EmissionType.PATTERNED,
                emissionPattern = emissionPattern,
                emissionDuration = 0.8f,              // Use 80% of time for emission
                assemblyApproachSpeed = 4.0f,
                assemblySpringStiffness = 2.5f,
                assemblySpringDamping = 0.8f,
                turbulenceStrength = 0.3f,            // Light turbulence
                duration = 2000                       // 2 seconds
            )
        }

        /**
         * Create a vibrant explosion effect with enhanced visuals
         */
        fun vibrantExplosion(): ParticleEffectConfig {
            return ParticleEffectConfig(
                particleSize = 5f,
                particleSizeVariation = 0.7f,
                particleCount = 12000,
                emissionType = EmissionType.INSTANT,
                disintegrationAngleVariation = 360f,
                disintegrationForce = 3.5f,
                turbulenceStrength = 1.8f,
                duration = 1800,
                // Enhanced visuals
                randomAlpha = true,
                scaleVariation = true,
                pulsateScale = true,
                pulsateAmplitude = 0.3f,
                pulsateFrequency = 4.0f,
                rotationVariation = 0.8f
            )
        }

        /**
         * Create a magical sparkle effect
         */
        fun magicalSparkle(): ParticleEffectConfig {
            return ParticleEffectConfig(
                particleSize = 3f,
                particleSizeVariation = 0.9f,
                particleCount = 15000,
                emissionType = EmissionType.PATTERNED,
                emissionPattern = EmissionPattern.CENTER_OUT,
                emissionDuration = 0.7f,
                disintegrationAngle = 90f,  // Upward
                disintegrationAngleVariation = 70f,  // Mostly upward
                disintegrationForce = 2.0f,
                disintegrationGravity = 3.0f,  // Light gravity
                turbulenceStrength = 2.0f,
                duration = 2500,
                // Enhanced visuals
                tintColor = Color(0, 150, 255, 255),  // Blue tint
                tintStrength = 0.4f,
                randomAlpha = true,
                alphaVariationSpeed = 0.8f,  // Twinkling effect
                scaleVariation = true,
                pulsateScale = true,
                randomizeLifetime = true
            )
        }

    }

}