package com.binissa.particlizeeffect.particlize.particle

import androidx.compose.ui.graphics.Color
import com.binissa.particlizeeffect.particlize.physics.Vector2
import kotlin.math.max

/**
 * Represents a single particle in the system.
 * Contains both visual properties and physics attributes.
 */
class Particle(
    // Original position in the source content
    var originalX: Float,
    var originalY: Float,
    
    // Current position (will be animated)
    var position: Vector2 = Vector2(originalX, originalY),
    
    // Velocity vector
    var velocity: Vector2 = Vector2(0f, 0f),
    
    // Acceleration vector (for physics)
    var acceleration: Vector2 = Vector2(0f, 0f),
    
    // Visual properties
    var color: Color,
    var alpha: Float = 1f,
    var scale: Float = 1f,
    var rotation: Float = 0f,
    
    // Shape of the particle
    var shape: ParticleShape = ParticleShape.CIRCLE,
    
    // Size of the particle
    var size: Float,
    
    // Lifetime and delay properties
    var lifetime: Float = 1f,
    var delay: Float = 0f,
    
    // Physics properties
    var mass: Float = 1f,
    var friction: Float = 0.98f
) {
    // Track if particle is active
    var isActive: Boolean = true
    
    /**
     * Updates particle physics
     */
    fun updatePhysics(deltaTime: Float) {
        // Apply acceleration
        velocity = velocity.plus(acceleration * deltaTime)
        
        // Apply friction
        velocity = velocity.times(friction)
        
        // Update position
        position = position.plus(velocity * deltaTime)
        
        // Reset acceleration for next frame
        acceleration = Vector2(0f, 0f)
    }
    
    /**
     * Applies a force to the particle
     */
    fun applyForce(force: Vector2) {
        // F = ma, so a = F/m
        acceleration = acceleration.plus(force / max(mass, 0.001f)) // Prevent division by zero
    }
    
    /**
     * Updates visual properties based on animation progress
     */
    fun updateVisuals(progress: Float, effect: ParticleEffect) {
        // Adjust progress based on particle delay
        val adjustedProgress = (progress - delay).coerceIn(0f, 1f)
        
        when (effect) {
            ParticleEffect.DISINTEGRATION -> {
                // Fade out and scale down as they move away
                alpha = 1f - adjustedProgress
                scale = 1f - (adjustedProgress * 0.5f)
                rotation += adjustedProgress * 180f
            }
            
            ParticleEffect.ASSEMBLY -> {
                // Fade in and scale up as they approach final position
                alpha = adjustedProgress
                scale = 0.5f + (adjustedProgress * 0.5f)
                rotation -= adjustedProgress * 180f
            }
        }
        
        // Deactivate when fully transparent
        if (alpha <= 0f) {
            isActive = false
        }
    }
}

/**
 * Enum defining the possible particle shapes
 */
enum class ParticleShape {
    CIRCLE,
    SQUARE,
    CUSTOM
}

/**
 * Enum defining the types of particle effects
 */
enum class ParticleEffect {
    DISINTEGRATION,
    ASSEMBLY
}