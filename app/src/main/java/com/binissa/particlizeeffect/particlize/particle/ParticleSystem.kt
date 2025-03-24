package com.binissa.particlizeeffect.particlize.particle

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import com.binissa.particlizeeffect.particlize.Animation.AnimationController
import com.binissa.particlizeeffect.particlize.config.AssemblyConfig
import com.binissa.particlizeeffect.particlize.config.DisintegrationConfig
import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.config.ParticleEffectConfig
import com.binissa.particlizeeffect.particlize.emission.EmissionController
import com.binissa.particlizeeffect.particlize.physics.AssemblyPhysicsEngine
import com.binissa.particlizeeffect.particlize.physics.DisintegrationPhysicsEngine
import com.binissa.particlizeeffect.particlize.physics.PhysicsBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random


class ParticleSystem(
    private val effectType: ParticleEffect,
    private val config: ParticleEffectConfig
) {
    // Component instances
    private val storage = ParticleStorage(config.particleCount)
    private val animationController = AnimationController(config)

    // Derived configurations
    private val particleConfig = ParticleConfig(
        size = config.particleSize,
        shape = config.particleShape,
        friction = config.friction
    )

    private val emissionConfig = EmissionConfig(
        type = config.emissionType,
        pattern = config.emissionPattern,
        particleCount = config.particleCount,
        durationFactor = config.emissionDuration
    )

    // Create appropriate physics based on effect type
    private val physics = when (effectType) {
        ParticleEffect.DISINTEGRATION -> createDisintegrationPhysics()
        ParticleEffect.ASSEMBLY -> createAssemblyPhysics()
    }

    /**
     * Create disintegration physics from config
     */
    private fun createDisintegrationPhysics(): PhysicsBehavior {
        return DisintegrationPhysicsEngine(
            particleConfig = particleConfig,
            disintegrationConfig = DisintegrationConfig().apply {
                angle = config.disintegrationAngle
                angleVariation = config.disintegrationAngleVariation
                explosionForce = config.disintegrationForce
                gravityForce = config.disintegrationGravity
                turbulenceStrength = config.turbulenceStrength
                turbulenceScale = config.turbulenceScale
                rotationSpeed = config.rotationSpeed
                fadeOutRate = config.fadeRate
                scaleDownFactor = config.scaleRate
            }
        )
    }

    /**
     * Create assembly physics from config
     */
    private fun createAssemblyPhysics(): PhysicsBehavior {
        return AssemblyPhysicsEngine(
            particleConfig = particleConfig,
            assemblyConfig = AssemblyConfig().apply {
                approachSpeed = config.assemblyApproachSpeed
                springStiffness = config.assemblySpringStiffness
                springDamping = config.assemblySpringDamping
                turbulenceStrength = config.turbulenceStrength
                turbulenceScale = config.turbulenceScale
                fadeInRate = config.fadeRate
            }
        )
    }
    private val emissionController = EmissionController(emissionConfig)


    private var physicsBehavior: PhysicsBehavior? = null

    // Current effect type
    private var currentEffect: ParticleEffect? = null

    // Content dimensions
    private var contentWidth = 0
    private var contentHeight = 0

    /**
     * Generate particles from bitmap
     */
    suspend fun generateParticles(bitmap: ImageBitmap, effect: ParticleEffect) {
        withContext(Dispatchers.Default) {
            // Store content dimensions
            contentWidth = bitmap.width
            contentHeight = bitmap.height

            // Set current effect
            currentEffect = effect

            physicsBehavior = PhysicsFactory.createPhysics(effect, particleConfig)

            // Reset storage
            storage.reset()

            // Create particles from bitmap
            createParticlesFromBitmap(bitmap, effect)

            // Calculate delays for patterned emission
            emissionController.calculateDelays(
                storage,
                emissionConfig.pattern,
                contentWidth,
                contentHeight,
                effect
            )

            // Log generation stats
            Log.d(
                "ModularParticleSystem",
                "Generated ${storage.particleCount} particles " +
                        "(${storage.getActiveCount()} active, ${storage.getPendingCount()} pending)"
            )

            // Start animation
            animationController.start()
        }
    }

    /**
     * Create initial particles from bitmap
     */
    private fun createParticlesFromBitmap(
        bitmap: ImageBitmap,
        effect: ParticleEffect
    ) {
        val pixelMap = bitmap.toPixelMap()
        val width = bitmap.width
        val height = bitmap.height

        // Sampling parameters
        val targetCount = min(storage.capacity, emissionConfig.particleCount)
        val samplingDensity = sqrt(targetCount / (width * height).toFloat())
        val stepSize = max(1, (1 / samplingDensity).toInt())

        // Generate particles
        for (x in 0 until width step stepSize) {
            for (y in 0 until height step stepSize) {
                if (storage.particleCount >= storage.capacity) break

                // Get pixel color
                val pixel = pixelMap[x, y]
                val alpha = pixel.alpha

                // Only create particles for non-transparent pixels
                if (alpha > 0.1f) {
                    // Create the particle
                    val index = storage.createParticle(
                        x = x.toFloat(),
                        y = y.toFloat(),
                        color = pixel,
                        shape = when (particleConfig.shape) {
                            ParticleShape.CIRCLE -> 0
                            ParticleShape.SQUARE -> 1
                            ParticleShape.CUSTOM -> 2
                        },
                        size = particleConfig.size * (0.7f + Random.nextFloat() * 0.6f)
                    )

                    if (index >= 0) {
                        // Set initial velocity
                        val angle = Random.nextFloat() * 2 * PI.toFloat()
                        val speed = particleConfig.velocityMagnitude * (0.5f + Random.nextFloat())
                        storage.velocityX[index] = cos(angle) * speed
                        storage.velocityY[index] = sin(angle) * speed

                        // Special setup for assembly effect
                        if (effect == ParticleEffect.ASSEMBLY) {
                            val distance = 100f + Random.nextFloat() * 100f
                            val offsetAngle = Random.nextFloat() * 2 * PI.toFloat()
                            storage.positionX[index] += cos(offsetAngle) * distance
                            storage.positionY[index] += sin(offsetAngle) * distance
                        }

                        // Initial active state (will be overridden by emission controller)
                        storage.active[index] = true
                    }
                }
            }

            if (storage.particleCount >= storage.capacity) break
        }
    }

    /**
     * Update animation state
     */
    fun updateAnimation(currentTime: Long): Boolean {
        val effect = currentEffect ?: return false
        val physics = physicsBehavior ?: return false

        // Update animation controller
        val animationRunning = animationController.update(currentTime)
        if (!animationRunning) return false

        // Get time since last frame
        val deltaTime = animationController.getDeltaTime(currentTime)

        // Process pending particles
        activatePendingParticles()

        // Update all active particles
        val activeIndices = storage.getActiveParticleIndices()
        for (i in activeIndices) {
            physics.updateParticle(
                storage,
                i,
                deltaTime,
                animationController.easedProgress
            )
        }

        // Continue if we have active or pending particles
        return storage.getActiveCount() > 0 || storage.getPendingCount() > 0
    }

    /**
     * Activate pending particles based on current progress
     */
    private fun activatePendingParticles() {
        val progress = animationController.rawProgress
        val pendingIndices = storage.getPendingParticleIndices()

        var activated = 0
        for (i in pendingIndices) {
            if (emissionController.shouldActivateParticle(storage.delays[i], progress)) {
                storage.activateParticle(i)
                activated++
            }
        }

        // Log when particles are activated
        if (activated > 0 && activated % 100 == 0) {
            Log.d("ModularParticleSystem", "Activated $activated particles at progress $progress")
        }
    }

    /**
     * Get data for rendering
     */
    fun getParticleData(): SimpleParticleData {
        return SimpleParticleData(
            numParticles = storage.particleCount,
            active = storage.active,
            posX = storage.positionX,
            posY = storage.positionY,
            colors = storage.colors,
            sizes = storage.sizes,
            alphas = storage.alphas,
            scales = storage.scales,
            rotations = storage.rotations,
            shapes = storage.shapes
        )
    }

    /**
     * Get animation completion percentage
     */
    fun getCompletionPercentage(): Float {
        return animationController.easedProgress
    }

    /**
     * Get content dimensions
     */
    fun getContentDimensions(): Pair<Int, Int> {
        return Pair(contentWidth, contentHeight)
    }
}