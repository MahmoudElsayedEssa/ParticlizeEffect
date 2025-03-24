package com.binissa.particlizeeffect.particlize.config

import com.binissa.particlizeeffect.particlize.emission.EmissionPattern
import com.binissa.particlizeeffect.particlize.emission.EmissionType
import com.binissa.particlizeeffect.particlize.particle.ParticleShape

/**
 * Simple density-based particle configuration
 */
data class ParticleConfig(
    // Basic particle properties
    val size: Float = 4f,          // Base size - will be affected by density setting
    val shape: ParticleShape = ParticleShape.CIRCLE,

    // Physics properties
    val velocityMagnitude: Float = 0.5f,
    val gravity: Float = 0.2f,
    val friction: Float = 0.98f,

    // Sub-configs
    val assembly: AssemblyConfig = AssemblyConfig(),
    val disintegration: DisintegrationConfig = DisintegrationConfig()
)

/**
 * Enhanced emission configuration with simple density control
 */
