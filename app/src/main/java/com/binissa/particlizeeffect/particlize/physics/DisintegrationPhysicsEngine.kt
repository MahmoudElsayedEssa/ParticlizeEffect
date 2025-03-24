package com.binissa.particlizeeffect.particlize.physics
import com.binissa.particlizeeffect.particlize.config.AssemblyConfig
import com.binissa.particlizeeffect.particlize.config.DisintegrationConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleStorage
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class DisintegrationPhysicsEngine(
    particleConfig: ParticleConfig,
    override val physicsConfig: DisintegrationConfig = DisintegrationConfig(),
    disintegrationConfig: DisintegrationConfig
) : PhysicsEngine(particleConfig, physicsConfig) {

    // Store individual particle angles for consistent movement
    private val particleAngles = FloatArray(10000) { 0f }
    private var initialized = false

    /**
     * Initialize particle angles
     */
    private fun initializeParticleAngles(storage: ParticleStorage) {
        if (initialized) return

        // For each particle, calculate an angle based on the configuration
        for (i in 0 until storage.particleCount) {
            // Apply variation to the base angle
            val variation = if (physicsConfig.angleVariation > 0) {
                (Random.nextFloat() - 0.5f) * physicsConfig.angleVariation
            } else 0f

            particleAngles[i] = physicsConfig.angle + variation
        }

        initialized = true
    }

    override fun updateParticle(
        storage: ParticleStorage,
        index: Int,
        deltaTime: Float,
        progress: Float
    ) {
        // Initialize angles if not already done
        if (!initialized) {
            initializeParticleAngles(storage)
        }

        // Visual updates
        storage.alphas[index] = max(0f, 1f - progress * physicsConfig.fadeOutRate)
        storage.scales[index] = max(0.1f, 1f - progress * physicsConfig.scaleDownFactor)

        // Apply rotation with some variation per particle
        val baseRotation = physicsConfig.rotationSpeed * 60f * deltaTime
        val particleVariation = (index % 10) / 10f * physicsConfig.rotationVariation
        storage.rotations[index] += baseRotation * (1f + particleVariation)

        // Apply physics forces

        // 1. Add gravity (increases with progress)
        val progressiveGravity = physicsConfig.gravityForce * (0.2f + progress * 0.8f)
        addDirectionalForce(physicsConfig.gravityAngle, progressiveGravity)

        // 2. Add turbulence (random movement)
        val time = progress * 5f  // Animation time for turbulence
        val turbulenceFactor = sin(progress * PI.toFloat())  // Peak in middle
        addTurbulence(storage, index, time)

        // 3. Add explosion/directional force (strong at start, weaker later)
        if (progress < physicsConfig.explosionDuration) {
            // Get this particle's angle
            val angle = particleAngles[index]

            // Calculate the explosion strength (decreases over time)
            val strength = physicsConfig.explosionForce *
                    (1f - progress / physicsConfig.explosionDuration)

            // Add directional force
            addDirectionalForce(angle, strength)

            // If directionality is less than 1, also add some force away from origin
            if (physicsConfig.directionality < 1f) {
                // Calculate direction from original position
                val dx = storage.positionX[index] - storage.originalX[index]
                val dy = storage.positionY[index] - storage.originalY[index]
                val distance = sqrt(dx * dx + dy * dy)

                if (distance > 0) {
                    // Normalized direction
                    val dirX = dx / distance
                    val dirY = dy / distance

                    // Use inverse directionality for this force
                    val radialStrength = strength * (1f - physicsConfig.directionality)
                    addForce(dirX * radialStrength, dirY * radialStrength)
                }
            }
        }

        // Apply forces to update velocity and position
        applyForces(storage, index, deltaTime)

        // Deactivate if transparent or off-screen
        if (storage.alphas[index] <= 0.01f) {
            storage.deactivateParticle(index)
        }
    }
}
