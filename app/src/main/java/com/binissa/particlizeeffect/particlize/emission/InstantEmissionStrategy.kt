package com.binissa.particlizeeffect.particlize.emission

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PixelMap
import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.particle.Particle
import com.binissa.particlizeeffect.particlize.particle.ParticleEffect
import com.binissa.particlizeeffect.particlize.physics.Vector2
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Strategy that emits all particles at once
 */
class InstantEmissionStrategy : EmissionStrategy {
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
        
        // Sample pixels from the bitmap
        for (x in 0 until width step stepSize) {
            for (y in 0 until height step stepSize) {
                // Get pixel color
                val pixel = pixelMap[x, y]
                val alpha = pixel.alpha
                
                // Only create particles for non-transparent pixels
                if (alpha > 0.1f) {
                    val initialPosition = Vector2(x.toFloat(), y.toFloat())
                    
                    // Create initial velocity based on effect
                    val initialVelocity = when (effect) {
                        ParticleEffect.DISINTEGRATION -> 
                            Vector2.random(particleConfig.velocityMagnitude)
                        ParticleEffect.ASSEMBLY -> {
                            // For assembly, start particles at random positions outside
                            initialPosition.x += Random.nextFloat() * 200 - 100
                            initialPosition.y += Random.nextFloat() * 200 - 100
                            Vector2.ZERO // No initial velocity for assembly
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
                        // All particles start immediately
                        delay = 0f,
                        friction = particleConfig.friction
                    )
                    
                    particles.add(particle)
                }
            }
        }
        
        return particles
    }
}