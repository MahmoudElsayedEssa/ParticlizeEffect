package com.binissa.particlizeeffect.particlize.Animation

import android.util.Log
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import com.binissa.particlizeeffect.particlize.config.AnimationConfig
import kotlin.math.min
import kotlin.random.Random

/**
 * Enhanced Animation Controller with per-particle timing and visual effects
 */
class AnimationController(val config: AnimationConfig) {
    // Timing state
    private var startTime = 0L
    private var lastUpdateTime = 0L
    private var pauseStartTime = 0L
    private var totalPausedTime = 0L

    // Animation state
    var isRunning = false
        private set

    var isPaused = false
        private set

    var rawProgress = 0f
        private set

    var easedProgress = 0f
        private set

    // Per-particle randomization
    private val particleTimeOffsets = mutableMapOf<Int, Float>()
    private val particleAlphaFactors = mutableMapOf<Int, Float>()
    private val particleScaleFactors = mutableMapOf<Int, Float>()
    private val particleFadeInFactors = mutableMapOf<Int, Float>()
    private val particleFadeOutFactors = mutableMapOf<Int, Float>()

    // For generating interesting visual patterns
    private val oscillationPhases = mutableMapOf<Int, Float>()

    // Custom easing function for smoother animations
    private val customEaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

    /**
     * Start the animation
     */
    fun start() {
        Log.d("EnhancedAnimation", "Starting animation with duration: ${config.duration}ms")
        startTime = System.currentTimeMillis()
        lastUpdateTime = startTime
        isRunning = true
        isPaused = false
        rawProgress = 0f
        easedProgress = 0f
        totalPausedTime = 0L

        // Clear any existing particle randomization
        particleTimeOffsets.clear()
        particleAlphaFactors.clear()
        particleScaleFactors.clear()
        particleFadeInFactors.clear()
        particleFadeOutFactors.clear()
        oscillationPhases.clear()
    }

    /**
     * Pause the animation
     */
    fun pause() {
        if (isRunning && !isPaused) {
            isPaused = true
            pauseStartTime = System.currentTimeMillis()
            Log.d("EnhancedAnimation", "Animation paused at progress: $easedProgress")
        }
    }

    /**
     * Resume the animation after pausing
     */
    fun resume() {
        if (isRunning && isPaused) {
            totalPausedTime += System.currentTimeMillis() - pauseStartTime
            isPaused = false
            lastUpdateTime = System.currentTimeMillis()
            Log.d("EnhancedAnimation", "Animation resumed at progress: $easedProgress")
        }
    }

    /**
     * Update animation state based on current time
     */
    fun update(currentTime: Long): Boolean {
        if (!isRunning) return false
        if (isPaused) return true  // Still running but paused

        // Calculate time since last frame
        val deltaTime = (currentTime - lastUpdateTime) / 1000f
        lastUpdateTime = currentTime

        // Calculate effective elapsed time (accounting for pauses)
        val effectiveElapsedTime = currentTime - startTime - totalPausedTime
        val duration = config.duration

        // Check if animation is complete
        if (effectiveElapsedTime >= duration) {
            // Handle repetition if configured
            if (config.repeatCount != 0) {
                // Handle infinite repetition (-1) or decrement count
                if (config.repeatCount > 0) {
                    config.repeatCount--
                }
                // Reset for next repetition but keep particle randomization
                startTime = currentTime - totalPausedTime
                Log.d("EnhancedAnimation", "Animation repeating, remaining repeats: ${config.repeatCount}")
                return true
            } else {
                isRunning = false
                rawProgress = 1f
                easedProgress = 1f
                Log.d("EnhancedAnimation", "Animation completed")
                return false
            }
        }

        // Calculate progress values
        rawProgress = (effectiveElapsedTime.toFloat() / duration).coerceIn(0f, 1f)

        // Apply easing with error handling
        try {
            // Use configured easing or fallback to custom ease-in-out
            easedProgress = config.easing.transform(rawProgress)
        } catch (e: Exception) {
            Log.e("EnhancedAnimation", "Error applying easing: ${e.message}")
            easedProgress = customEaseInOut.transform(rawProgress) // Fallback
        }

        return true
    }

    /**
     * Get time elapsed since last frame in seconds
     */
    fun getDeltaTime(currentTime: Long): Float {
        return min(0.05f, (currentTime - lastUpdateTime) / 1000f)
    }

    /**
     * Reset the animation controller
     */
    fun reset() {
        Log.d("EnhancedAnimation", "Animation reset")
        isRunning = false
        isPaused = false
        rawProgress = 0f
        easedProgress = 0f

        // Keep particle randomization for consistent behavior if reused
    }

    /**
     * Get per-particle progress with randomized timing offset
     */
    fun getParticleProgress(particleIndex: Int): Float {
        // Get or create timing offset for this particle
        val offset = particleTimeOffsets.getOrPut(particleIndex) {
            if (config.randomizeTimings) {
                // Random offset within configured range
                Random.nextFloat() * config.timingVariation * 2f - config.timingVariation
            } else {
                0f // No offset if randomization disabled
            }
        }

        // Apply offset and clamp to valid range
        return (easedProgress + offset).coerceIn(0f, 1f)
    }

    /**
     * Get alpha factor for a particle (0.0-1.0)
     * This combines base progress with randomized variations
     */
    fun getParticleAlpha(particleIndex: Int, isDisintegration: Boolean): Float {
        // Get particle-specific progress (possibly with time offset)
        val particleProgress = getParticleProgress(particleIndex)

        // Get or create alpha randomization factor
        val alphaFactor = particleAlphaFactors.getOrPut(particleIndex) {
            0.8f + Random.nextFloat() * 0.4f // 0.8-1.2 range
        }

        // Get or create fade rate factors
        val fadeInFactor = particleFadeInFactors.getOrPut(particleIndex) {
            0.9f + Random.nextFloat() * 0.2f // 0.9-1.1 range
        }

        val fadeOutFactor = particleFadeOutFactors.getOrPut(particleIndex) {
            0.9f + Random.nextFloat() * 0.2f // 0.9-1.1 range
        }

        // For disintegration, alpha decreases with progress
        // For assembly, alpha increases with progress
        return if (isDisintegration) {
            // Fade out with randomization
            ((1f - particleProgress * fadeOutFactor) * alphaFactor).coerceIn(0f, 1f)
        } else {
            // Fade in with randomization
            (particleProgress * fadeInFactor * alphaFactor).coerceIn(0f, 1f)
        }
    }

    /**
     * Get scale factor for a particle
     * This combines base progress with randomized variations
     */
    fun getParticleScale(particleIndex: Int, isDisintegration: Boolean): Float {
        // Get particle-specific progress (possibly with time offset)
        val particleProgress = getParticleProgress(particleIndex)

        // Get or create scale randomization factor
        val scaleFactor = particleScaleFactors.getOrPut(particleIndex) {
            0.85f + Random.nextFloat() * 0.3f // 0.85-1.15 range
        }

        // For disintegration: start at 100%, shrink to 70%
        // For assembly: start at 50%, grow to 100%
        return if (isDisintegration) {
            // Scale down with randomization
            (1f - particleProgress * 0.3f) * scaleFactor
        } else {
            // Scale up with randomization
            (0.5f + particleProgress * 0.5f) * scaleFactor
        }
    }

    /**
     * Get oscillation value for interesting visual effects
     * Returns -1 to 1 value based on progress and particle-specific phase
     */
    fun getParticleOscillation(particleIndex: Int, frequency: Float = 4f): Float {
        // Get or create oscillation phase for this particle
        val phase = oscillationPhases.getOrPut(particleIndex) {
            Random.nextFloat() * 2f * Math.PI.toFloat()
        }

        // Calculate oscillation based on progress, phase and frequency
        return kotlin.math.sin(rawProgress * frequency * Math.PI.toFloat() * 2f + phase)
    }
}