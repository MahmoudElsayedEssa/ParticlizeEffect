package com.binissa.particlizeeffect.particlize.config

/**
 * Configuration specific to assembly physics
 */
data class AssemblyConfig(
    // Attraction properties
    val initialAttractionStrength: Float = 0.05f,    // How strongly particles are initially attracted to target positions
    val finalAttractionStrength: Float = 0.25f,      // How strongly particles are attracted at end of animation

    // Visual properties
    val initialScale: Float = 0.5f,                 // Starting scale for particles
    val finalScale: Float = 1.0f,                   // Ending scale for particles
    val fadeInRate: Float = 1.0f,                   // How quickly particles fade in (1.0 = normal, >1.0 = faster)
    val rotationSpeed: Float = 1.0f,                // Base rotation speed

    // Natural motion effects
    val oscillationStrength: Float = 0.3f,          // Strength of oscillation effect
    val oscillationFrequency: Float = 4.0f,         // Frequency of oscillation
    val turbulenceStrength: Float = 0.2f,          // Strength of random turbulence

    // Randomization ranges
    val alphaVariationRange: Float = 0.3f,          // Random variation in alpha values
    val scaleVariationRange: Float = 0.25f,         // Random variation in scale values
    val rotationVariationRange: Float = 0.5f        // Random variation in rotation speed
)
