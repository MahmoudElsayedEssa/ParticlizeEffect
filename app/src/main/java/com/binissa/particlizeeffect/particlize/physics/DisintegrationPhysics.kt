package com.binissa.particlizeeffect.particlize.physics

import android.util.Log
import com.binissa.particlizeeffect.particlize.Animation.AnimationController
import com.binissa.particlizeeffect.particlize.config.BoundaryBehavior
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleStorage
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Disintegration Physics with enhanced visual effects
 */
class DisintegrationPhysics(private val config: ParticleConfig) : PhysicsBehavior {
    // Access disintegration config
    private val disintegrationConfig = config.disintegration

    // Store per-particle randomness
    private val particleRandomness = mutableMapOf<Int, ParticleParams>()

    // Global state
    private var windTime = 0f
    private var updateCount = 0

    // Reference to animation controller for rich visual effects
    private var animationController: AnimationController? = null

    data class ParticleParams(
        val angle: Float,
        val speed: Float,
        val alphaVariation: Float,
        val scaleVariation: Float,
        val rotationSpeed: Float,
        val rotationDirection: Float,
        val turbulenceOffset: Vector2,
        val lifespanFactor: Float,
        val startingPosition: Vector2
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

        // Calculate content dimensions for normalized coordinates
        val contentWidth = max(1f, storage.originalX.maxOrNull() ?: 1f)
        val contentHeight = max(1f, storage.originalY.maxOrNull() ?: 1f)

        // Get or create particle randomization using config values
        val params = particleRandomness.getOrPut(index) {
            // Calculate explosion direction based on directionality config
            val baseAngle: Float
            if (disintegrationConfig.explosionDirectionality > 0f) {
                // Mix between random direction and fixed direction based on directionality
                val randomAngle = Random.nextFloat() * 2f * PI.toFloat()
                val fixedAngleRad = disintegrationConfig.explosionDirection * PI.toFloat() / 180f
                baseAngle = randomAngle * (1f - disintegrationConfig.explosionDirectionality) +
                        fixedAngleRad * disintegrationConfig.explosionDirectionality
            } else {
                // Fully random direction
                baseAngle = Random.nextFloat() * 2f * PI.toFloat()
            }

            // Velocity magnitude from config with some randomization
            val speedFactor = 0.8f + Random.nextFloat() * 0.4f // 0.8-1.2x
            val baseSpeed = config.velocityMagnitude * speedFactor * disintegrationConfig.explosionStrength

            // Apply config-based randomization
            ParticleParams(
                angle = baseAngle,
                speed = baseSpeed,
                alphaVariation = Random.nextFloat() * disintegrationConfig.alphaVariationRange,
                scaleVariation = Random.nextFloat() * disintegrationConfig.scaleVariationRange - (disintegrationConfig.scaleVariationRange / 2f),
                rotationSpeed = disintegrationConfig.rotationSpeed * (0.5f + Random.nextFloat() * disintegrationConfig.rotationVariationRange),
                rotationDirection = if (Random.nextBoolean()) 1f else -1f,
                turbulenceOffset = Vector2(Random.nextFloat() * 1000, Random.nextFloat() * 1000),
                lifespanFactor = 1f - (Random.nextFloat() * disintegrationConfig.lifespanVariationRange),
                startingPosition = Vector2(storage.originalX[index], storage.originalY[index])
            )
        }

        // Use per-particle progress if animation controller is available
        val particleProgress = if (animationController != null) {
            animationController!!.getParticleProgress(index)
        } else {
            progress * params.lifespanFactor
        }.coerceIn(0f, 1f)

        // Calculate base movement factors - still using direct position calculation for reliability
        // but incorporating more config values
        val dirX = cos(params.angle)
        val dirY = sin(params.angle)

        // Apply explosion effect (stronger at beginning, following config duration)
        val explosionPhase = min(1f, progress / disintegrationConfig.explosionDuration)
        val explosionFactor = (1f - explosionPhase)

        // Base movement from explosion
        var moveX = dirX * params.speed * explosionFactor
        var moveY = dirY * params.speed * explosionFactor

        // Apply gravity from config
        moveY += disintegrationConfig.gravity * particleProgress * 2f

        // Apply wind if enabled
        if (disintegrationConfig.windEnabled) {
            // Update wind time based on deltaTime
            windTime += clampedDeltaTime

            // Calculate wind direction from config
            val windAngle = disintegrationConfig.windDirection * PI.toFloat() / 180f
            val windDirX = cos(windAngle)
            val windDirY = sin(windAngle)

            // Calculate gustiness effect from config
            val gustFactor = if (disintegrationConfig.windGustiness > 0) {
                1f + sin(windTime * 1.5f) * disintegrationConfig.windGustiness
            } else 1f

            // Apply wind force with gustiness
            val windForce = disintegrationConfig.windStrength * gustFactor
            moveX += windDirX * windForce * particleProgress
            moveY += windDirY * windForce * particleProgress
        }

        // Apply vortex if enabled
        if (disintegrationConfig.vortexEnabled) {
            // Get particle position in normalized coordinates
            val normalizedX = storage.positionX[index] / contentWidth
            val normalizedY = storage.positionY[index] / contentHeight

            // Vector from particle to vortex center
            val toVortexX = disintegrationConfig.vortexPosition.x - normalizedX
            val toVortexY = disintegrationConfig.vortexPosition.y - normalizedY

            // Distance to vortex center (normalized)
            val distance = sqrt(toVortexX * toVortexX + toVortexY * toVortexY)

            // Apply vortex effect if within radius
            if (distance < disintegrationConfig.vortexRadius) {
                // Calculate vortex strength based on distance (stronger near center)
                val strengthFactor = (1f - (distance / disintegrationConfig.vortexRadius).pow(disintegrationConfig.vortexDecay))
                    .coerceIn(0f, 1f)

                // Calculate perpendicular direction for circular motion
                val angle = atan2(toVortexY, toVortexX)
                val perpX = -sin(angle) * disintegrationConfig.vortexDirection
                val perpY = cos(angle) * disintegrationConfig.vortexDirection

                // Add vortex movement
                moveX += perpX * disintegrationConfig.vortexStrength * strengthFactor * particleProgress
                moveY += perpY * disintegrationConfig.vortexStrength * strengthFactor * particleProgress
            }
        }

        // Apply turbulence based on config
        if (disintegrationConfig.turbulenceStrength > 0) {
            val turbX = sin(params.turbulenceOffset.x + particleProgress * disintegrationConfig.turbulenceFrequency)
            val turbY = cos(params.turbulenceOffset.y + particleProgress * disintegrationConfig.turbulenceFrequency)

            moveX += turbX * disintegrationConfig.turbulenceStrength * (1f - particleProgress)
            moveY += turbY * disintegrationConfig.turbulenceStrength * (1f - particleProgress)
        }

        // Apply oscillation effect if animation controller provides it
        if (animationController != null && animationController!!.config.addOscillation) {
            val oscillation = animationController!!.getParticleOscillation(index) *
                    animationController!!.config.oscillationStrength

            // Apply oscillation perpendicular to movement direction
            val perpX = -dirY
            val perpY = dirX

            moveX += perpX * oscillation * (1f - particleProgress)
            moveY += perpY * oscillation * (1f - particleProgress)
        }

        // CALCULATE POSITION DIRECTLY based on initial position, direction and progress

        // Base distance to move over entire animation
        val maxDistance = 200f * disintegrationConfig.explosionStrength.coerceIn(0.5f, 5f)

        // Calculate current position directly
        val startX = params.startingPosition.x
        val startY = params.startingPosition.y

        // Calculate displacement based on all factors and progress
        val displacementX = moveX * maxDistance * particleProgress
        val displacementY = moveY * maxDistance * particleProgress

        // Set position directly
        storage.positionX[index] = startX + displacementX
        storage.positionY[index] = startY + displacementY

        // Apply friction to movement (visual - slows down toward the end)
        moveX *= disintegrationConfig.friction
        moveY *= disintegrationConfig.friction

        // Update visual properties based on config

        // Alpha (fade out) - use animation controller for richer effects if available
        if (animationController != null && animationController!!.config.randomizeAlpha) {
            storage.alphas[index] = animationController!!.getParticleAlpha(index, true)
        } else {
            // Fallback to basic alpha calculation
            storage.alphas[index] = (1f - particleProgress * disintegrationConfig.fadeOutRate) *
                    (1f - params.alphaVariation * particleProgress)
        }

        // Scale (shrink) - use animation controller for richer effects if available
        if (animationController != null && animationController!!.config.randomizeScale) {
            storage.scales[index] = animationController!!.getParticleScale(index, true)
        } else {
            // Fallback to basic scale calculation
            storage.scales[index] = 1f - (particleProgress * disintegrationConfig.scaleDownFactor * (1f + params.scaleVariation))
        }

        // Rotation
        storage.rotations[index] += params.rotationSpeed * params.rotationDirection *
                clampedDeltaTime * 60f

        // Handle boundaries based on config
        if (disintegrationConfig.boundaryBehavior != BoundaryBehavior.NONE) {
            handleBoundaries(
                storage, index, contentWidth, contentHeight,
                disintegrationConfig.boundaryBehavior,
                disintegrationConfig.boundaryElasticity
            )
        }

        // Deactivate if fully transparent
        if (storage.alphas[index] <= 0.01f) {
            storage.deactivateParticle(index)
        }
    }

    /**
     * Handle boundary interactions based on config
     */
    private fun handleBoundaries(
        storage: ParticleStorage,
        index: Int,
        width: Float,
        height: Float,
        behavior: BoundaryBehavior,
        elasticity: Float
    ) {
        val posX = storage.positionX[index]
        val posY = storage.positionY[index]

        when (behavior) {
            BoundaryBehavior.BOUNCE -> {
                // Bounce off edges
                if (posX < 0) {
                    storage.positionX[index] = -posX * elasticity
                } else if (posX > width) {
                    storage.positionX[index] = width - (posX - width) * elasticity
                }

                if (posY < 0) {
                    storage.positionY[index] = -posY * elasticity
                } else if (posY > height) {
                    storage.positionY[index] = height - (posY - height) * elasticity
                }
            }

            BoundaryBehavior.WRAP -> {
                // Wrap around edges
                if (posX < 0) storage.positionX[index] = width + (posX % width)
                else if (posX > width) storage.positionX[index] = posX % width

                if (posY < 0) storage.positionY[index] = height + (posY % height)
                else if (posY > height) storage.positionY[index] = posY % height
            }

            BoundaryBehavior.DESTROY -> {
                // Destroy if outside boundaries
                if (posX < -20 || posX > width + 20 || posY < -20 || posY > height + 20) {
                    storage.deactivateParticle(index)
                }
            }

            BoundaryBehavior.NONE -> { /* No action needed */ }
        }
    }

    /**
     * Reset internal physics state
     */
    fun reset() {
        particleRandomness.clear()
        windTime = 0f
        updateCount = 0
    }
}