package com.binissa.particlizeeffect.particlize.physics
import com.binissa.particlizeeffect.particlize.config.AssemblyConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleStorage
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class AssemblyPhysicsEngine(
    particleConfig: ParticleConfig,
    override val physicsConfig: AssemblyConfig = AssemblyConfig(),
    assemblyConfig: AssemblyConfig
) : PhysicsEngine(particleConfig, physicsConfig) {

    // Random seeds for each particle
    private val particleSeeds = FloatArray(10000) { Random.nextFloat() * 1000f }

    override fun updateParticle(
        storage: ParticleStorage,
        index: Int,
        deltaTime: Float,
        progress: Float
    ) {
        // Visual updates - fade in and scale up with progress
        storage.alphas[index] = min(1f, progress * physicsConfig.fadeInRate)
        storage.scales[index] = physicsConfig.initialScale +
                (1f - physicsConfig.initialScale) * progress

        // Gradually stop rotation as we approach destination
        val rotationFactor = max(0f, 1f - progress * physicsConfig.rotationFadeOutRate)
        if (rotationFactor > 0) {
            storage.rotations[index] += 30f * deltaTime * rotationFactor
        }

        // Initial velocity damping (only at beginning)
        if (progress < 0.2f) {
            storage.velocityX[index] *= physicsConfig.initialVelocityDamping
            storage.velocityY[index] *= physicsConfig.initialVelocityDamping
        }

        // Phase-based behaviors
        when {
            // Gathering phase - particles move roughly toward destination with turbulence
            progress < physicsConfig.gatheringPhaseEnd -> {
                // Strong initial pull toward target
                val strength = physicsConfig.approachSpeed * 0.5f
                addAttractionForce(
                    storage, index,
                    storage.originalX[index], storage.originalY[index],
                    strength
                )

                // Add turbulence for interesting motion
                val seed = particleSeeds[index % particleSeeds.size]
                val turbulenceStrength = 2.0f * (1f - progress / physicsConfig.gatheringPhaseEnd)
                addTurbulence(storage, index, seed + progress * 3f)
            }

            // Spring phase - particles behave like springs, oscillating around target
            progress < physicsConfig.springPhaseEnd -> {
                // Calculate normalized progress within this phase
                val phaseProgress = (progress - physicsConfig.gatheringPhaseEnd) /
                        (physicsConfig.springPhaseEnd - physicsConfig.gatheringPhaseEnd)

                // Spring stiffness increases with progress
                val stiffness = physicsConfig.springStiffness * (0.5f + phaseProgress * 0.5f)

                // Spring damping increases with progress for smoother finish
                val damping = physicsConfig.springDamping * (0.5f + phaseProgress * 0.5f)

                // Apply spring force toward target
                addSpringForce(
                    storage, index,
                    storage.originalX[index], storage.originalY[index],
                    stiffness, damping
                )

                // Slight turbulence for liveliness
                val seed = particleSeeds[index % particleSeeds.size]
                val turbulenceStrength = 0.5f * (1f - phaseProgress)
                addTurbulence(storage, index, seed + progress * 5f)
            }

            // Final settling phase - particles smoothly move to exact position
            else -> {
                // Calculate distance to target
                val dx = storage.originalX[index] - storage.positionX[index]
                val dy = storage.originalY[index] - storage.positionY[index]
                val distSq = dx * dx + dy * dy

                if (distSq < physicsConfig.finalSnapDistance * physicsConfig.finalSnapDistance) {
                    // If close enough, snap to final position
                    storage.positionX[index] = storage.originalX[index]
                    storage.positionY[index] = storage.originalY[index]
                    storage.velocityX[index] = 0f
                    storage.velocityY[index] = 0f
                } else {
                    // Strong direct pull to target
                    val t = min(1f, deltaTime * 10f)  // Interpolation factor
                    storage.positionX[index] = lerp(storage.positionX[index], storage.originalX[index], t)
                    storage.positionY[index] = lerp(storage.positionY[index], storage.originalY[index], t)

                    // Dampen velocity
                    storage.velocityX[index] *= 0.9f
                    storage.velocityY[index] *= 0.9f
                }
            }
        }

        // Apply accumulated forces to update velocity and position
        applyForces(storage, index, deltaTime)
    }

    /**
     * Linear interpolation between values
     */
    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }
}