package com.binissa.particlizeeffect.particlize.config

/**
 * Base configuration for all physics behaviors
 */
open class PhysicsConfig {
    var dampingFactor: Float = 0.98f
    var minVelocity: Float = 0.001f
    
    // Turbulence settings
    var turbulenceEnabled: Boolean = true
    var turbulenceScale: Float = 0.1f
    var turbulenceStrength: Float = 1.0f
    
    // Force multipliers
    var forceMultiplier: Float = 1.0f
    var velocityMultiplier: Float = 1.0f
}

/**
 * Configuration specific to disintegration physics
 */
class DisintegrationConfig : PhysicsConfig() {
    // Directional control
    var angle: Float = 90f  // Angle in degrees (90 = up, 0 = right, 180 = left, 270 = down)
    var angleVariation: Float = 360f  // How much variation in angles (360 = full random)
    var directionality: Float = 0.5f  // How strongly to enforce the direction (0-1)
    
    // Force controls
    var explosionForce: Float = 2.0f
    var explosionDuration: Float = 0.3f  // How long the explosion lasts (0-1 in progress)
    var gravityForce: Float = 9.8f
    var gravityAngle: Float = 270f  // Direction of gravity (270 = down)
    
    // Visual controls
    var rotationSpeed: Float = 2.0f
    var rotationVariation: Float = 1.0f  // Variation in rotation speed between particles
    var scaleDownFactor: Float = 0.5f  // How much particles shrink
    var fadeOutRate: Float = 1.2f  // How quickly particles fade out
}

/**
 * Configuration specific to assembly physics
 */
class AssemblyConfig : PhysicsConfig() {
    // Phase timing (as fractions of total progress)
    var gatheringPhaseEnd: Float = 0.4f
    var springPhaseEnd: Float = 0.8f
    
    // Force controls
    var initialVelocityDamping: Float = 0.9f
    var approachSpeed: Float = 5.0f
    var springStiffness: Float = 3.0f
    var springDamping: Float = 0.7f
    var finalSnapDistance: Float = 5.0f
    
    // Visual controls
    var initialScale: Float = 0.3f
    var rotationFadeOutRate: Float = 2.0f  // How quickly rotation stops
    var fadeInRate: Float = 1.2f  // How quickly particles fade in
}