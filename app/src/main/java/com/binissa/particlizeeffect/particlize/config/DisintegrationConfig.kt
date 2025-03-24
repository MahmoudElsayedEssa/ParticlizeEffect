package com.binissa.particlizeeffect.particlize.config

import androidx.compose.ui.graphics.Color
import com.binissa.particlizeeffect.particlize.physics.Vector2
import kotlin.math.PI

/**
 * Enhanced DisintegrationConfig with advanced physics parameters
 */
data class DisintegrationConfig(
    // Basic physics
    val gravity: Float = 0.2f,                      // Gravity force pulling particles down
    val friction: Float = 0.98f,                    // Air friction slowing particles

    // Explosion properties
    val explosionStrength: Float = 1.0f,            // Initial explosion force magnitude
    val explosionDuration: Float = 0.3f,            // How long the explosion effect lasts (0-1 in progress)
    val explosionDirectionality: Float = 0.0f,      // 0=omnidirectional, 1=highly directional
    val explosionDirection: Float = 270f,           // Direction of explosion in degrees (if directional)

    // Wind properties
    val windEnabled: Boolean = false,               // Whether wind is applied
    val windStrength: Float = 0.5f,                 // Base strength of wind force
    val windDirection: Float = 0f,                  // Direction of wind in degrees (0=right, 90=down)
    val windGustiness: Float = 0.2f,                // How much wind strength varies over time
    val windTurbulence: Float = 0.1f,               // Small-scale directional variation in wind

    // Vortex properties
    val vortexEnabled: Boolean = false,             // Whether vortex forces are applied
    val vortexStrength: Float = 0.5f,               // Strength of vortex force
    val vortexPosition: Vector2 = Vector2(0.5f, 0.5f), // Position of vortex (normalized 0-1 coords)
    val vortexRadius: Float = 0.3f,                 // Radius of maximum vortex effect
    val vortexDecay: Float = 2.0f,                  // How quickly vortex weakens with distance
    val vortexDirection: Float = 1f,                // 1=clockwise, -1=counterclockwise

    // Force field properties
    val forceFieldEnabled: Boolean = false,         // Whether force field is applied
    val forceFieldStrength: Float = 1.0f,           // Strength of force field
    val forceFieldPosition: Vector2 = Vector2(0.5f, 0.5f), // Position of force field (normalized 0-1 coords)
    val forceFieldRadius: Float = 0.5f,             // Radius of maximum force field effect
    val forceFieldType: ForceFieldType = ForceFieldType.REPEL, // Type of force field

    // Advanced physics properties
    val dragCoefficient: Float = 0.01f,             // Air resistance based on velocity squared
    val particleMass: Float = 1.0f,                 // Mass of particles (affects all forces)
    val terminalVelocity: Float = 10f,              // Maximum velocity magnitude

    // Boundaries
    val boundaryBehavior: BoundaryBehavior = BoundaryBehavior.NONE, // How particles interact with boundaries
    val boundaryElasticity: Float = 0.5f,           // Bounce factor when hitting boundaries

    // Visual properties
    val rotationSpeed: Float = 1.0f,                // Base rotation speed
    val scaleDownFactor: Float = 0.3f,              // How much particles shrink
    val fadeOutRate: Float = 1.0f,                  // How quickly particles fade out

    // Randomization ranges
    val turbulenceStrength: Float = 0.15f,          // Background turbulence
    val turbulenceFrequency: Float = 5.0f,          // Frequency of turbulence
    val alphaVariationRange: Float = 0.3f,          // Random variation in alpha
    val scaleVariationRange: Float = 0.4f,          // Random variation in scale
    val lifespanVariationRange: Float = 0.2f,       // Random variation in particle lifespan
    val rotationVariationRange: Float = 0.5f        // Random variation in rotation speed
)

/**
 * Types of force fields that can be applied to particles
 */
enum class ForceFieldType {
    ATTRACT,    // Pull particles toward center
    REPEL,      // Push particles away from center
    ORBIT       // Make particles orbit around center
}

/**
 * Defines how particles behave when they reach boundaries
 */
enum class BoundaryBehavior {
    NONE,       // No boundary effects
    BOUNCE,     // Particles bounce off boundaries
    WRAP,       // Particles wrap around to opposite side
    DESTROY     // Particles are destroyed when they hit boundaries
}