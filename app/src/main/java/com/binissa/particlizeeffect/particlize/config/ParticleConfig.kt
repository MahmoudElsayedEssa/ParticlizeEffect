package com.binissa.particlizeeffect.particlize.config

import androidx.compose.ui.graphics.Color
import com.binissa.particlizeeffect.particlize.particle.ParticleShape

/**
 * Configuration for individual particle appearance and behavior
 */
data class ParticleConfig(
    // Visual properties
    val size: Float = 4f,
    val shape: ParticleShape = ParticleShape.CIRCLE,
    
    // Physics properties
    val velocityMagnitude: Float = 0.5f,
    val gravity: Float = 0.2f,
    val friction: Float = 0.98f,
    
    // Custom color transform (null means use original pixel colors)
    val colorTransform: ((Color) -> Color)? = null
) {
    companion object {
        val Default = ParticleConfig()
        
        // Preset configurations
        val Explosive = ParticleConfig(
            size = 3f,
            velocityMagnitude = 1.5f,
            gravity = 0.4f
        )
        
        val Gentle = ParticleConfig(
            size = 5f,
            velocityMagnitude = 0.3f,
            gravity = 0.1f,
            friction = 0.99f
        )
        
        val Squared = ParticleConfig(
            shape = ParticleShape.SQUARE,
            size = 4f,
            velocityMagnitude = 0.6f
        )
    }
}