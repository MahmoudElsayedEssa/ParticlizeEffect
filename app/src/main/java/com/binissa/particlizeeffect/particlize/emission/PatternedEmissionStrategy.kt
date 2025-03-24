package com.binissa.particlizeeffect.particlize.emission

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PixelMap
import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.particle.Particle
import com.binissa.particlizeeffect.particlize.particle.ParticleEffect
import com.binissa.particlizeeffect.particlize.physics.Vector2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Strategy that emits particles in a pattern
 */
class PatternedEmissionStrategy(
    private val pattern: EmissionPattern
) : EmissionStrategy {
    override fun generateParticles(
        bitmap: ImageBitmap,
        pixelMap: PixelMap,
        particleConfig: ParticleConfig,
        emissionConfig: EmissionConfig,
        effect: ParticleEffect
    ): List<Particle> {
        val particles = mutableListOf<Particle>()
        val width = bitmap.width
        val height = bitmap.height
        
        // Determine sampling density based on particle count
        val particleCount = emissionConfig.particleCount
        val samplingDensity = sqrt(particleCount / (width * height).toFloat())
        val stepSize = max(1, (1 / samplingDensity).toInt())
        
        // Calculate particle delays properly
        // First, generate all pattern values and find their range
        val patternValues = mutableMapOf<Pair<Int, Int>, Float>()
        var minValue = Float.MAX_VALUE
        var maxValue = Float.MIN_VALUE
        
        for (x in 0 until width step stepSize) {
            for (y in 0 until height step stepSize) {
                val patternValue = calculatePatternValue(x, y, width, height, pattern)
                patternValues[Pair(x, y)] = patternValue
                
                if (patternValue < minValue) minValue = patternValue
                if (patternValue > maxValue) maxValue = patternValue
            }
        }
        
        // Calculate the value range for normalization
        val valueRange = maxValue - minValue
        
        // Sample pixels and create particles
        for (x in 0 until width step stepSize) {
            for (y in 0 until height step stepSize) {
                // Get pixel color
                val pixel = pixelMap[x, y]
                val alpha = pixel.alpha
                
                // Only create particles for non-transparent pixels
                if (alpha > 0.1f) {
                    // Normalize the pattern value to 0-1 range for delay
                    val normalizedValue = (patternValues[Pair(x, y)]!! - minValue) / valueRange
                    
                    // Calculate delay based on normalized pattern value
                    val delay = when (effect) {
                        ParticleEffect.DISINTEGRATION -> normalizedValue
                        ParticleEffect.ASSEMBLY -> 1f - normalizedValue
                    } * emissionConfig.durationFactor
                    
                    val initialPosition = Vector2(x.toFloat(), y.toFloat())
                    
                    // Create initial velocity
                    val initialVelocity = when (effect) {
                        ParticleEffect.DISINTEGRATION -> 
                            Vector2.random(particleConfig.velocityMagnitude)
                        ParticleEffect.ASSEMBLY -> {
                            // For assembly, start particles at positions based on the pattern
                            val distance = normalizedValue * 200f
                            val angle = Random.nextFloat() * 2 * PI.toFloat()
                            initialPosition.x += cos(angle) * distance
                            initialPosition.y += sin(angle) * distance
                            Vector2.ZERO
                        }
                    }
                    
                    val particle = Particle(
                        originalX = x.toFloat(),
                        originalY = y.toFloat(),
                        position = initialPosition,
                        velocity = initialVelocity,
                        color = pixel,
                        shape = particleConfig.shape,
                        size = particleConfig.size * (0.5f + Random.nextFloat()),
                        delay = delay,
                        friction = particleConfig.friction
                    )
                    
                    particles.add(particle)
                }
            }
        }
        
        return particles
    }
    
    /**
     * Calculate the raw pattern value for a given position
     */
    private fun calculatePatternValue(
        x: Int, 
        y: Int, 
        width: Int, 
        height: Int,
        pattern: EmissionPattern
    ): Float {
        return when (pattern) {
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
    }
}