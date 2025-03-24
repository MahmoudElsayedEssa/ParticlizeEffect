package com.binissa.particlizeeffect.particlize.physics

import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.config.PhysicsConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleStorage
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

abstract class PhysicsEngine(
    protected val particleConfig: ParticleConfig,
    protected open val physicsConfig: PhysicsConfig = PhysicsConfig()
) : PhysicsBehavior {
    // Force accumulators
    protected val tempForceX = FloatArray(1) { 0f }
    protected val tempForceY = FloatArray(1) { 0f }

    /**
     * Apply accumulated forces to particle
     */
    protected fun applyForces(
        storage: ParticleStorage,
        index: Int,
        deltaTime: Float
    ) {
        // Apply accumulated forces (F = ma → a = F/m)
        val mass = 1.0f  // Default mass, could be particle-specific
        val accelX = tempForceX[0] / mass * physicsConfig.forceMultiplier
        val accelY = tempForceY[0] / mass * physicsConfig.forceMultiplier

        // Update velocity using acceleration
        storage.velocityX[index] += accelX * deltaTime
        storage.velocityY[index] += accelY * deltaTime

        // Apply velocity multiplier
        storage.velocityX[index] *= physicsConfig.velocityMultiplier
        storage.velocityY[index] *= physicsConfig.velocityMultiplier

        // Apply damping/friction
        storage.velocityX[index] *= physicsConfig.dampingFactor
        storage.velocityY[index] *= physicsConfig.dampingFactor

        // Set very small velocities to zero to avoid numerical drift
        if (abs(storage.velocityX[index]) < physicsConfig.minVelocity) storage.velocityX[index] = 0f
        if (abs(storage.velocityY[index]) < physicsConfig.minVelocity) storage.velocityY[index] = 0f

        // Update position using velocity
        storage.positionX[index] += storage.velocityX[index] * deltaTime
        storage.positionY[index] += storage.velocityY[index] * deltaTime

        // Reset force accumulators for next frame
        tempForceX[0] = 0f
        tempForceY[0] = 0f
    }

    /**
     * Add a force vector to the accumulated forces
     */
    protected fun addForce(forceX: Float, forceY: Float) {
        tempForceX[0] += forceX
        tempForceY[0] += forceY
    }

    /**
     * Add a force in a specific direction and magnitude
     */
    protected fun addDirectionalForce(angle: Float, magnitude: Float) {
        val radians = Math.toRadians(angle.toDouble()).toFloat()
        val forceX = cos(radians) * magnitude
        val forceY = sin(radians) * magnitude
        addForce(forceX, forceY)
    }

    /**
     * Add attractive force toward a point
     */
    protected fun addAttractionForce(
        storage: ParticleStorage,
        index: Int,
        targetX: Float,
        targetY: Float,
        strength: Float,
        minDistance: Float = 1f
    ) {
        // Calculate direction vector
        val dx = targetX - storage.positionX[index]
        val dy = targetY - storage.positionY[index]

        // Calculate distance (avoid division by zero)
        val distanceSquared = dx * dx + dy * dy
        if (distanceSquared < minDistance * minDistance) return

        val distance = sqrt(distanceSquared)

        // Calculate normalized direction
        val dirX = dx / distance
        val dirY = dy / distance

        // Attraction force is stronger when farther away
        val forceStrength = strength

        addForce(dirX * forceStrength, dirY * forceStrength)
    }

    /**
     * Add a spring force toward a point
     */
    protected fun addSpringForce(
        storage: ParticleStorage,
        index: Int,
        anchorX: Float,
        anchorY: Float,
        stiffness: Float,
        damping: Float = 0.5f
    ) {
        // Calculate displacement from rest position
        val dx = storage.positionX[index] - anchorX
        val dy = storage.positionY[index] - anchorY

        // Calculate spring force (F = -k*x)
        val springForceX = -stiffness * dx
        val springForceY = -stiffness * dy

        // Calculate damping force (F = -c*v)
        val dampingForceX = -damping * storage.velocityX[index]
        val dampingForceY = -damping * storage.velocityY[index]

        // Add forces
        addForce(springForceX + dampingForceX, springForceY + dampingForceY)
    }

    /**
     * Add turbulence force if enabled
     */
    protected fun addTurbulence(
        storage: ParticleStorage,
        index: Int,
        time: Float
    ) {
        if (!physicsConfig.turbulenceEnabled) return

        // Use particle position and time to generate pseudo-random turbulence
        val noiseX = noise(
            storage.positionX[index] * physicsConfig.turbulenceScale,
            storage.positionY[index] * physicsConfig.turbulenceScale,
            time
        ) * 2f - 1f

        val noiseY = noise(
            storage.positionX[index] * physicsConfig.turbulenceScale + 100f,
            storage.positionY[index] * physicsConfig.turbulenceScale + 100f,
            time + 100f
        ) * 2f - 1f

        addForce(
            noiseX * physicsConfig.turbulenceStrength,
            noiseY * physicsConfig.turbulenceStrength
        )
    }

    /**
     * Simple noise function (could be replaced with a proper Perlin/Simplex noise)
     */
    private fun noise(x: Float, y: Float, z: Float): Float {
        // This is a placeholder - ideally you'd use a real noise function
        val h = (1234.5f * sin(x * 0.0123f + y * 0.0456f + z * 0.0789f)) % 1f
        return h * h
    }
}
