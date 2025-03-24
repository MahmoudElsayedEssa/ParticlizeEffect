package com.binissa.particlizeeffect.particlize.particle

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import com.binissa.particlizeeffect.particlize.Animation.AnimationController
import com.binissa.particlizeeffect.particlize.config.AnimationConfig
import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.emission.EmissionController
import com.binissa.particlizeeffect.particlize.emission.EmissionPattern
import com.binissa.particlizeeffect.particlize.physics.AssemblyPhysics
import com.binissa.particlizeeffect.particlize.physics.DisintegrationPhysics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Updated Particle System with amazing effects support
 */
class ParticleSystem(
    private val particleConfig: ParticleConfig,
    private val emissionConfig: EmissionConfig,
    private val animationConfig: AnimationConfig
) {
    // Component instances
    private val storage = ParticleStorage(emissionConfig.particleCount)
    private val disintegrationPhysics = DisintegrationPhysics(particleConfig)
    private val assemblyPhysics = AssemblyPhysics(particleConfig)
    private val emissionController = EmissionController(emissionConfig)
    private val animationController = AnimationController(animationConfig)

    // Current effect type
    private var currentEffect: ParticleEffect? = null

    // Content dimensions
    private var contentWidth = 0
    private var contentHeight = 0

    // Statistics
    private var activeParticleCount = 0
    private var frameCount = 0

    init {
        // Connect animation controller to physics engines
        disintegrationPhysics.setAnimationController(animationController)
        assemblyPhysics.setAnimationController(animationController)
    }

    /**
     * Generate particles from bitmap with enhanced visual properties
     */
    suspend fun generateParticles(bitmap: ImageBitmap, effect: ParticleEffect) {
        withContext(Dispatchers.Default) {
            Log.d("ParticleSystem", "Generating particles for effect: $effect")

            // Store content dimensions
            contentWidth = bitmap.width
            contentHeight = bitmap.height

            // Set current effect
            currentEffect = effect

            // Reset components
            disintegrationPhysics.reset()
            assemblyPhysics.reset()
            storage.reset()
            frameCount = 0

            // Create particles from bitmap
            createParticlesFromBitmap(bitmap, effect)

            // Initialize particles with effect-specific settings
            if (effect == ParticleEffect.DISINTEGRATION) {
                initializeDisintegrationParticles()
            } else {
                initializeAssemblyParticles()
            }

            // Calculate emission delays
            emissionController.calculateDelays(
                storage,
                emissionConfig.pattern,
                contentWidth,
                contentHeight,
                effect
            )

            // Log generation stats
            activeParticleCount = storage.getActiveCount()
            Log.d("ParticleSystem", "Generated ${storage.particleCount} particles " +
                    "(${activeParticleCount} active, ${storage.getPendingCount()} pending)")

            // Start animation
            animationController.start()
        }
    }

    /**
     * Initialize special settings for disintegration particles
     */
    private fun initializeDisintegrationParticles() {
        Log.d("ParticleSystem", "Initializing disintegration particles")

        for (i in 0 until storage.particleCount) {
            // Apply random visual properties using EmissionController
            emissionController.applyRandomVisualProperties(storage, i)

            // Initialize random velocities for more natural explosion
            emissionController.initializeVelocities(storage, i, particleConfig.velocityMagnitude)

            // Start with full alpha for disintegration effect
            if (!emissionConfig.randomAlphas) {
                storage.alphas[i] = 1.0f
            }

            // Start with full scale
            if (!emissionConfig.randomSizes) {
                storage.scales[i] = 1.0f
            }
        }
    }

    /**
     * Initialize special settings for assembly particles
     */
    private fun initializeAssemblyParticles() {
        Log.d("ParticleSystem", "Initializing assembly particles")

        for (i in 0 until storage.particleCount) {
            // Apply random visual properties
            emissionController.applyRandomVisualProperties(storage, i)

            // For assembly, start with low alpha
            storage.alphas[i] = 0.3f + Random.nextFloat() * 0.2f

            // Start with smaller scale
            storage.scales[i] = 0.4f + Random.nextFloat() * 0.2f

            // Calculate random offset angle
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()

            // Random distance - between 150-300 pixels
            val distance = 150f + Random.nextFloat() * 150f

            // Calculate offset
            val offsetX = kotlin.math.cos(angle) * distance
            val offsetY = kotlin.math.sin(angle) * distance

            // Move particle away from target position
            val origX = storage.positionX[i]
            val origY = storage.positionY[i]

            storage.positionX[i] = origX + offsetX
            storage.positionY[i] = origY + offsetY
        }
    }

    /**
     * Create particles from bitmap
     */
    private fun createParticlesFromBitmap(
        bitmap: ImageBitmap,
        effect: ParticleEffect
    ) {
        val pixelMap = bitmap.toPixelMap()
        val width = bitmap.width
        val height = bitmap.height

        // Sampling parameters to get good coverage
        val targetCount = min(storage.capacity, emissionConfig.particleCount)
        val samplingDensity = sqrt(targetCount / (width * height).toFloat())
        val stepSize = max(1, (1 / samplingDensity).toInt())

        Log.d("ParticleSystem", "Creating particles with step size: $stepSize")

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
                }
            }

            if (storage.particleCount >= storage.capacity) break
        }

        Log.d("ParticleSystem", "Created ${storage.particleCount} particles")
    }

    /**
     * Update animation state with enhanced physics
     */
    fun updateAnimation(currentTime: Long): Boolean {
        val effect = currentEffect ?: return false

        // Update frame counter
        frameCount++

        // Debug logging occasionally
        val shouldLog = frameCount % 60 == 0

        // Update animation controller
        val animationRunning = animationController.update(currentTime)
        if (!animationRunning) {
            Log.d("ParticleSystem", "Animation completed after $frameCount frames")
            return false
        }

        // Get time since last frame
        val deltaTime = animationController.getDeltaTime(currentTime).coerceIn(0.001f, 0.1f)

        // Process pending particles
        activatePendingParticles()

        // Choose physics behavior based on effect
        val physicsBehavior = when (effect) {
            ParticleEffect.DISINTEGRATION -> disintegrationPhysics
            ParticleEffect.ASSEMBLY -> assemblyPhysics
        }

        // Get active particles
        val activeIndices = storage.getActiveParticleIndices()
        activeParticleCount = activeIndices.size

        // Log stats occasionally
        if (shouldLog) {
            Log.d("ParticleSystem", "Frame $frameCount: ${activeParticleCount} active particles, " +
                    "progress: ${animationController.easedProgress}")
        }

        // Update all active particles
        for (i in activeIndices) {
            physicsBehavior.updateParticle(
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
        if (activated > 0) {
            Log.d("ParticleSystem", "Activated $activated particles at progress $progress")
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