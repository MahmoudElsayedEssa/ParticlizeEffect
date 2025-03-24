package com.binissa.particlizeeffect.particlize.emission

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleEffect
import com.binissa.particlizeeffect.particlize.particle.ParticleStorage
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Enhanced Emission Controller with random visual properties
 */
class EmissionController(
    private val config: EmissionConfig
) {
    /**
     * Apply random visual properties during particle creation
     */
    fun applyRandomVisualProperties(
        storage: ParticleStorage,
        index: Int
    ) {
        // Apply random size variation if enabled
        if (config.randomSizes) {
            // Random size variation (0.6x to 1.4x the base size)
            val sizeVariation = 0.6f + Random.nextFloat() * 0.8f
            storage.sizes[index] *= sizeVariation
        }

        // Apply random alpha if enabled
        if (config.randomAlphas) {
            // Random alpha variation (0.6 to 1.0)
            val alphaVariation = 0.6f + Random.nextFloat() * 0.4f
            storage.alphas[index] = alphaVariation
        }

        // Apply random colors if enabled
        if (config.randomizeColors) {
            // Get current color as base
            val baseColor = storage.colors[index]

            // Apply random hue shift, maintaining saturation and value
            val hueShift = Random.nextFloat() * 40f - 20f  // -20 to +20 degrees
            val saturationShift = Random.nextFloat() * 0.2f - 0.1f  // -0.1 to +0.1
            val brightnessShift = Random.nextFloat() * 0.2f - 0.1f  // -0.1 to +0.1

            // Use a simple color modification - in a real app you'd use HSV conversion
            val r = (baseColor.red + brightnessShift).coerceIn(0f, 1f)
            val g = (baseColor.green + brightnessShift).coerceIn(0f, 1f)
            val b = (baseColor.blue + brightnessShift).coerceIn(0f, 1f)

            storage.colors[index] = Color(r, g, b, baseColor.alpha)
        }

        // Apply random rotation if enabled
        if (config.randomRotations) {
            // Random initial rotation (0-360 degrees)
            storage.rotations[index] = Random.nextFloat() * 360f
        }
    }

    /**
     * Initialize particle initial velocities with good randomization
     */
    fun initializeVelocities(
        storage: ParticleStorage,
        index: Int,
        baseVelocity: Float
    ) {
        // Generate truly random angle with good distribution
        val angle = Random.nextFloat() * 2 * Math.PI.toFloat()

        // Random speed variation (0.5x to 1.5x base velocity)
        val speedVariation = 0.5f + Random.nextFloat() * 1.0f
        val speed = baseVelocity * speedVariation

        // Calculate velocity components
        val vx = kotlin.math.cos(angle) * speed
        val vy = kotlin.math.sin(angle) * speed

        // Store velocities
        storage.velocityX[index] = vx
        storage.velocityY[index] = vy
    }

    /**
     * Checks if particles should be activated based on their delay and current progress
     */
    fun shouldActivateParticle(delay: Float, progress: Float): Boolean {
        // For instant emission, always return true
        if (config.type == EmissionType.INSTANT) {
            return true
        }

        // For patterned emission, check if progress has reached delay
        return progress >= delay
    }

    /**
     * Calculates delay values for patterned emission
     */
    fun calculateDelays(
        storage: ParticleStorage,
        pattern: EmissionPattern,
        width: Int,
        height: Int,
        effect: ParticleEffect
    ) {
        // Skip for instant emission - all particles should be active immediately
        if (config.type == EmissionType.INSTANT) {
            for (i in 0 until storage.particleCount) {
                storage.delays[i] = 0f
                storage.active[i] = true
                storage.pending[i] = false
            }
            return
        }

        // Calculate pattern values
        val patternValues = calculatePatternValues(
            storage, width, height, pattern
        )

        // Apply delays based on effect
        for (i in 0 until storage.particleCount) {
            val patternValue = patternValues[i]

            // Add some randomness to delays if enabled
            var finalDelay = patternValue
            if (config.randomizeDelays) {
                // Add up to ±30% random variation to delay
                val randomVariation = 1.0f - 0.3f + Random.nextFloat() * 0.6f
                finalDelay = (patternValue * randomVariation).coerceIn(0f, 1f)
            }

            storage.delays[i] = when (effect) {
                ParticleEffect.DISINTEGRATION -> finalDelay
                ParticleEffect.ASSEMBLY -> 1f - finalDelay
            } * config.durationFactor

            // Ensure some particles start active immediately
            val forceActive = (i < storage.particleCount * 0.05f)

            if (storage.delays[i] > 0 && !forceActive) {
                storage.pending[i] = true
                storage.active[i] = false
            } else {
                storage.active[i] = true
                storage.pending[i] = false
            }
        }
    }

    /**
     * Calculates normalized pattern values for all particles
     */
    private fun calculatePatternValues(
        storage: ParticleStorage,
        width: Int,
        height: Int,
        pattern: EmissionPattern
    ): FloatArray {
        val result = FloatArray(storage.particleCount)
        var minValue = Float.MAX_VALUE
        var maxValue = Float.MIN_VALUE

        // Calculate raw pattern values
        for (i in 0 until storage.particleCount) {
            val x = storage.originalX[i].toInt()
            val y = storage.originalY[i].toInt()

            result[i] = when (pattern) {
                EmissionPattern.LEFT_TO_RIGHT -> x.toFloat() / width
                EmissionPattern.RIGHT_TO_LEFT -> 1f - (x.toFloat() / width)
                EmissionPattern.TOP_TO_BOTTOM -> y.toFloat() / height
                EmissionPattern.BOTTOM_TO_TOP -> 1f - (y.toFloat() / height)
                EmissionPattern.CENTER_OUT -> {
                    val centerX = width / 2f
                    val centerY = height / 2f
                    val distanceToCenter = sqrt(
                        (x - centerX).pow(2) + (y - centerY).pow(2)
                    )
                    val maxDistance = sqrt(
                        (width / 2f).pow(2) + (height / 2f).pow(2)
                    )
                    distanceToCenter / maxDistance
                }
                EmissionPattern.OUTSIDE_IN -> {
                    val centerX = width / 2f
                    val centerY = height / 2f
                    val distanceToCenter = sqrt(
                        (x - centerX).pow(2) + (y - centerY).pow(2)
                    )
                    val maxDistance = sqrt(
                        (width / 2f).pow(2) + (height / 2f).pow(2)
                    )
                    1f - (distanceToCenter / maxDistance)
                }
            }

            if (result[i] < minValue) minValue = result[i]
            if (result[i] > maxValue) maxValue = result[i]
        }

        // Normalize to 0-1 range
        val valueRange = maxValue - minValue
        if (valueRange > 0) {
            for (i in 0 until storage.particleCount) {
                result[i] = (result[i] - minValue) / valueRange
            }
        }

        return result
    }
}