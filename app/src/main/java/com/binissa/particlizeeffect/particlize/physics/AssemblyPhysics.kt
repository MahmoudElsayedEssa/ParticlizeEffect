package com.binissa.particlizeeffect.particlize.physics

import android.util.Log
import com.binissa.particlizeeffect.particlize.Animation.AnimationController
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleStorage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Assembly Physics with enhanced visual effects
 */
class AssemblyPhysics(private val config: ParticleConfig) : PhysicsBehavior {
    // Access assembly config
    private val assemblyConfig = config.assembly

    // Store per-particle randomness
    private val particleRandomness = mutableMapOf<Int, ParticleParams>()

    // Global state
    private var updateCount = 0

    // Reference to animation controller for rich visual effects
    private var animationController: AnimationController? = null

    data class ParticleParams(
        val startingOffset: Vector2,
        val alphaVariation: Float,
        val scaleVariation: Float,
        val oscillationPhase: Float,
        val oscillationFrequency: Float,
        val turbulenceOffset: Vector2,
        val rotationVariation: Float
    )

    /**
     * Set animation controller reference for rich visual effects
     */
    fun setAnimationController(controller: AnimationController) {
        this.animationController = controller
    }

    override fun updateParticle(
        storage: ParticleStorage,
        index: Int,
        deltaTime: Float,
        progress: Float
    ) {
        updateCount++

        // Ensure reasonable deltaTime
        val clampedDeltaTime = deltaTime.coerceIn(0.001f, 0.1f)

        // Get or create particle-specific randomization using config values
        val params = particleRandomness.getOrPut(index) {
            // Calculate random offset angle
            val angle = Random.nextFloat() * 2 * PI.toFloat()

            // Offset distance - between 150-300 pixels or more based on config
            val offsetMagnitude = 150f + Random.nextFloat() * 150f

            // Calculate offset vector
            val offsetX = cos(angle) * offsetMagnitude
            val offsetY = sin(angle) * offsetMagnitude

            ParticleParams(
                startingOffset = Vector2(offsetX, offsetY),
                alphaVariation = Random.nextFloat() * assemblyConfig.alphaVariationRange - (assemblyConfig.alphaVariationRange / 2f),
                scaleVariation = Random.nextFloat() * assemblyConfig.scaleVariationRange - (assemblyConfig.scaleVariationRange / 2f),
                oscillationPhase = Random.nextFloat() * 2 * PI.toFloat(),
                oscillationFrequency = assemblyConfig.oscillationFrequency * (0.8f + Random.nextFloat() * 0.4f),
                turbulenceOffset = Vector2(Random.nextFloat() * 1000, Random.nextFloat() * 1000),
                rotationVariation = Random.nextFloat() * assemblyConfig.rotationVariationRange - (assemblyConfig.rotationVariationRange / 2f)
            )
        }

        // Target position (where particle should end up)
        val targetX = storage.originalX[index]
        val targetY = storage.originalY[index]

        // Calculate start position (only on first update)
        if (storage.velocityX[index] == 0f && storage.velocityY[index] == 0f) {
            // First time - store the starting position offset in velocity array (reused as storage)
            storage.velocityX[index] = params.startingOffset.x
            storage.velocityY[index] = params.startingOffset.y

            // Set initial position
            storage.positionX[index] = targetX + params.startingOffset.x
            storage.positionY[index] = targetY + params.startingOffset.y
        }

        // Starting position (far from target)
        val startX = targetX + storage.velocityX[index]
        val startY = targetY + storage.velocityY[index]

        // Use per-particle progress if animation controller is available
        val particleProgress = if (animationController != null) {
            animationController!!.getParticleProgress(index)
        } else {
            progress
        }.coerceIn(0f, 1f)

        // Apply custom easing for more natural assembly
        // Start slow, accelerate, then slow down at the end
        val easedProgress = if (particleProgress < 0.5f) {
            2f * particleProgress * particleProgress // Ease in
        } else {
            1f - ((-2f * particleProgress + 2f) * (-2f * particleProgress + 2f)) / 2f // Ease out
        }

        // Calculate base position by interpolating from start to target
        var posX = startX + (targetX - startX) * easedProgress
        var posY = startY + (targetY - startY) * easedProgress

        // Apply oscillation effect based on config
        if (assemblyConfig.oscillationStrength > 0) {
            val oscillation = if (animationController != null && animationController!!.config.addOscillation) {
                // Use animation controller's oscillation if available
                animationController!!.getParticleOscillation(index, params.oscillationFrequency) *
                        animationController!!.config.oscillationStrength
            } else {
                // Fallback to basic oscillation
                sin(particleProgress * params.oscillationFrequency + params.oscillationPhase) *
                        assemblyConfig.oscillationStrength
            } * (1f - easedProgress) // Fade out oscillation as we approach target

            // Add perpendicular oscillation
            val dirX = targetX - startX
            val dirY = targetY - startY
            val dirLength = sqrt(dirX * dirX + dirY * dirY)

            if (dirLength > 0.01f) {
                // Calculate perpendicular vector
                val perpX = -dirY / dirLength
                val perpY = dirX / dirLength

                // Apply oscillation perpendicular to movement direction
                posX += perpX * oscillation * 20f
                posY += perpY * oscillation * 20f
            }
        }

        // Apply turbulence based on config
        if (assemblyConfig.turbulenceStrength > 0) {
            val turbX = sin(params.turbulenceOffset.x + particleProgress * 5f)
            val turbY = cos(params.turbulenceOffset.y + particleProgress * 5f)

            // Turbulence decreases as particles get closer to target
            val turbFactor = assemblyConfig.turbulenceStrength * (1f - easedProgress)

            posX += turbX * turbFactor * 10f
            posY += turbY * turbFactor * 10f
        }

        // Set position directly
        storage.positionX[index] = posX
        storage.positionY[index] = posY

        // Visual updates based on config

        // Alpha (fade in) - use animation controller for richer effects if available
        if (animationController != null && animationController!!.config.randomizeAlpha) {
            storage.alphas[index] = animationController!!.getParticleAlpha(index, false)
        } else {
            // Fallback to basic alpha calculation
            val alphaBase = assemblyConfig.initialScale + (1f - assemblyConfig.initialScale) * easedProgress
            storage.alphas[index] = (alphaBase * assemblyConfig.fadeInRate * (1f + params.alphaVariation)).coerceIn(0.1f, 1f)
        }

        // Scale (grow) - use animation controller for richer effects if available
        if (animationController != null && animationController!!.config.randomizeScale) {
            storage.scales[index] = animationController!!.getParticleScale(index, false)
        } else {
            // Fallback to basic scale calculation
            val scaleBase = assemblyConfig.initialScale + (assemblyConfig.finalScale - assemblyConfig.initialScale) * easedProgress
            storage.scales[index] = scaleBase * (1f + params.scaleVariation)
        }

        // Rotation - slows down as progress increases
        storage.rotations[index] += assemblyConfig.rotationSpeed *
                (1f - easedProgress) *
                params.rotationVariation *
                clampedDeltaTime * 60f

        // Final snap: if very close to target at the end of animation, snap to it
        if (particleProgress > 0.95f) {
            val dx = targetX - posX
            val dy = targetY - posY
            val distSq = dx * dx + dy * dy
            if (distSq < 5f * 5f) {
                storage.positionX[index] = targetX
                storage.positionY[index] = targetY
                storage.alphas[index] = 1f
                storage.scales[index] = 1f
            }
        }
    }

    /**
     * Reset internal physics state
     */
    fun reset() {
        particleRandomness.clear()
        updateCount = 0
    }
}