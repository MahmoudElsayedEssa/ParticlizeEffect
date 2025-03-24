package com.binissa.particlizeeffect.particlize.emission

import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleEffect
import com.binissa.particlizeeffect.particlize.particle.ParticleStorage
import kotlin.math.pow
import kotlin.math.sqrt

class EmissionController(
    private val config: EmissionConfig
) {
    /**
     * Checks if particles should be activated based on their delay and current progress
     */
    fun shouldActivateParticle(delay: Float, progress: Float): Boolean {
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
        // Skip for instant emission
        if (config.type == EmissionType.INSTANT) {
            return
        }

        // Calculate pattern values
        val patternValues = calculatePatternValues(
            storage, width, height, pattern
        )

        // Apply delays based on effect
        for (i in 0 until storage.particleCount) {
            val patternValue = patternValues[i]

            storage.delays[i] = when (effect) {
                ParticleEffect.DISINTEGRATION -> patternValue
                ParticleEffect.ASSEMBLY -> 1f - patternValue
            } * config.durationFactor

            // Mark as pending if there's a delay
            if (storage.delays[i] > 0) {
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