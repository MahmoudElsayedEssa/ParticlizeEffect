package com.binissa.particlizeeffect.particlize.Animation

import androidx.compose.ui.graphics.Color
import com.binissa.particlizeeffect.particlize.config.ParticleEffectConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleEffect
import com.binissa.particlizeeffect.particlize.particle.ParticleStorage
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random


/**
 * Enhanced animation controller with advanced visual effects
 */
class AnimationController(private val config: ParticleEffectConfig) {
    // Animation timing
    private var startTime = 0L
    private var lastUpdateTime = 0L

    // Animation state
    var isRunning = false
        private set

    var rawProgress = 0f
        private set

    var easedProgress = 0f
        private set

    // Random seeds for consistent variation per particle
    private val randomScales = FloatArray(10000) {
        lerp(
            config.scaleVariationRange.first,
            config.scaleVariationRange.second,
            Random.nextFloat()
        )
    }

    private val randomAlphas = FloatArray(10000) {
        lerp(
            config.randomAlphaRange.first,
            config.randomAlphaRange.second,
            Random.nextFloat()
        )
    }

    private val randomRotations = FloatArray(10000) {
        config.rotationSpeed * (1f + (Random.nextFloat() - 0.5f) * 2f * config.rotationVariation) *
                (if (config.randomRotationDirection && Random.nextBoolean()) 1f else -1f)
    }

    private val randomLifetimes = FloatArray(10000) {
        if (config.randomizeLifetime) {
            lerp(
                config.lifetimeRange.first,
                config.lifetimeRange.second,
                Random.nextFloat()
            )
        } else 1f
    }

    /**
     * Start the animation
     */
    fun start() {
        startTime = System.currentTimeMillis()
        lastUpdateTime = startTime
        isRunning = true
        rawProgress = 0f
        easedProgress = 0f
    }

    /**
     * Update animation progress based on time
     */
    fun update(currentTime: Long): Boolean {
        if (!isRunning) return false

        // Calculate time since last frame
        val deltaTime = (currentTime - lastUpdateTime) / 1000f
        lastUpdateTime = currentTime

        // Calculate total progress
        val elapsedTime = currentTime - startTime
        val duration = config.duration

        // Check if animation is complete
        if (elapsedTime >= duration) {
            isRunning = false
            rawProgress = 1f
            easedProgress = 1f
            return false
        }

        // Calculate progress values
        rawProgress = (elapsedTime.toFloat() / duration).coerceIn(0f, 1f)
        easedProgress = config.easing.transform(rawProgress)

        return true
    }

    /**
     * Update particle visual properties based on animation progress
     */
    fun updateParticleVisuals(
        storage: ParticleStorage,
        index: Int,
        effect: ParticleEffect
    ) {
        val baseIndex = index % 10000  // Ensure we stay within our random arrays

        // Apply color transformations if needed
        if (!config.useOriginalColors && config.tintColor != null) {
            val originalColor = storage.colors[index]
            val tint = config.tintColor!!

            // Apply tint with configured strength
            storage.colors[index] = blendColors(originalColor, tint, config.tintStrength)
        }

        // Alpha/transparency effects
        updateAlpha(storage, index, baseIndex, effect)

        // Scale effects
        updateScale(storage, index, baseIndex, effect)

        // Rotation effects
        updateRotation(storage, index, baseIndex)

        // Apply lifetime variations
        if (config.randomizeLifetime) {
            // For disintegration, particles with shorter lifetimes fade out faster
            // For assembly, particles with longer lifetimes start appearing earlier
            val lifetime = randomLifetimes[baseIndex]

            if (effect == ParticleEffect.DISINTEGRATION) {
                // Shorter lifetime = reach end state faster
                if (rawProgress > lifetime) {
                    storage.deactivateParticle(index)
                }
            }
        }
    }

    /**
     * Update particle alpha value
     */
    private fun updateAlpha(
        storage: ParticleStorage,
        index: Int,
        baseIndex: Int,
        effect: ParticleEffect
    ) {
        // Base alpha value depends on effect and progress
        val baseAlpha = when (effect) {
            ParticleEffect.DISINTEGRATION -> 1f - easedProgress  // Fade out
            ParticleEffect.ASSEMBLY -> easedProgress  // Fade in
        }

        // Apply random initial alpha if enabled
        var finalAlpha = if (config.randomAlpha) {
            baseAlpha * randomAlphas[baseIndex]
        } else {
            baseAlpha
        }

        // Apply alpha variation over time if enabled
        if (config.alphaVariationSpeed > 0) {
            val variation = sin(rawProgress * config.alphaVariationSpeed * 10f + baseIndex * 0.1f) * 0.3f + 0.7f
            finalAlpha *= variation
        }

        // Apply to particle
        storage.alphas[index] = finalAlpha.coerceIn(0f, 1f)

        // Deactivate if completely transparent
        if (finalAlpha <= 0.01f) {
            storage.deactivateParticle(index)
        }
    }

    /**
     * Update particle scale
     */
    private fun updateScale(
        storage: ParticleStorage,
        index: Int,
        baseIndex: Int,
        effect: ParticleEffect
    ) {
        // Base scale depends on effect and progress
        val baseScale = when (effect) {
            ParticleEffect.DISINTEGRATION -> {
                // Start at 1.0, shrink to finalScale
                lerp(1.0f, config.finalScale, easedProgress)
            }
            ParticleEffect.ASSEMBLY -> {
                // Start at initialScale, grow to 1.0
                lerp(config.initialScale, 1.0f, easedProgress)
            }
        }

        // Apply random scale variation if enabled
        var finalScale = if (config.scaleVariation) {
            baseScale * randomScales[baseIndex]
        } else {
            baseScale
        }

        // Apply pulsating effect if enabled
        if (config.pulsateScale) {
            val pulsate = sin(rawProgress * config.pulsateFrequency * 2 * PI.toFloat() + baseIndex * 0.1f)
            finalScale *= 1f + (pulsate * config.pulsateAmplitude)
        }

        // Apply to particle
        storage.scales[index] = finalScale.coerceAtLeast(0.1f)  // Prevent negative/zero scale
    }

    /**
     * Update particle rotation
     */
    private fun updateRotation(
        storage: ParticleStorage,
        index: Int,
        baseIndex: Int
    ) {
        if (!config.enableRotation) return

        // Get rotation speed with variation
        val rotationAmount = randomRotations[baseIndex] * (lastUpdateTime - startTime) / 1000f

        // Apply rotation (continuous over time)
        storage.rotations[index] = (storage.rotations[index] + rotationAmount) % 360f
    }

    /**
     * Get the time since the last frame in seconds
     */
    fun getDeltaTime(currentTime: Long): Float {
        return min(0.05f, (currentTime - lastUpdateTime) / 1000f)
    }

    /**
     * Reset the animation
     */
    fun reset() {
        isRunning = false
        rawProgress = 0f
        easedProgress = 0f
    }

    /**
     * Blend two colors with the given factor
     */
    private fun blendColors(color1: Color, color2: Color, factor: Float): Color {
        val r = lerp(color1.red, color2.red, factor)
        val g = lerp(color1.green, color2.green, factor)
        val b = lerp(color1.blue, color2.blue, factor)
        val a = lerp(color1.alpha, color2.alpha, factor)
        return Color(r, g, b, a)
    }

    /**
     * Linear interpolation between values
     */
    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }
}