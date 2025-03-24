package com.binissa.particlizeeffect.particlize.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import com.binissa.particlizeeffect.particlize.config.ParticleEffectConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleSystem
import com.binissa.particlizeeffect.particlize.particle.ParticleEffect
import com.binissa.particlizeeffect.particlize.rendering.RenderEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Controller with improved reliability
 */
class ParticlizeController {
    // Internal state
    internal var componentSize = mutableStateOf<Size?>(null)

    // Effect state tracking
    private val state = mutableStateOf(State.NONE)
    private val needsRedraw = mutableStateOf(false)

    // The particle system and renderer
    private val renderEngine = RenderEngine()
    private var particleSystem: ParticleSystem? = null


    // Coroutine scope and job
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var animationJob: Job? = null

    // Effect parameters
    private var currentEffect: ParticleEffect? = null

    // Callbacks
    private var onEffectComplete: (() -> Unit)? = null

    // Captured content
    private var contentBitmap: ImageBitmap? = null

    // Parameters for pending start
    private var pendingStartParams: StartParams? = null
    private var frameCount = 0



    private var effectConfig = ParticleEffectConfig()

    fun setConfig(config: ParticleEffectConfig) {
        effectConfig = config
    }

    /**
     * Data class to hold start parameters
     */
    private data class StartParams(
        val context: Context,
        val effect: ParticleEffect,
        val config: ParticleEffectConfig,
        val onComplete: () -> Unit
    )

    private fun createParticleSystem(params: StartParams) {
        // Create particle system with full config
        particleSystem = ParticleSystem(
            params.effect,
            params.config
        )
    }
    /**
     *
     * Called when content has been captured
     */
    fun onContentCaptured(bitmap: ImageBitmap) {
        Log.d("ParticlizeController", "Content captured: ${bitmap.width}x${bitmap.height}")

        if (state.value != State.CAPTURING || pendingStartParams == null) {
            return
        }

        // Store the captured bitmap
        contentBitmap = bitmap

        // Start the effect
        val params = pendingStartParams!!

        // Create the particle system
        particleSystem = ParticleSystem(
            particleConfig = params.particleConfig,
            emissionConfig = params.emissionConfig,
            animationConfig = params.animationConfig
        )

        // Start the effect
        currentEffect = params.effect
        onEffectComplete = params.onComplete

        // Generate particles and start animation
        coroutineScope.launch {
            particleSystem?.generateParticles(contentBitmap!!, params.effect)
            Log.d("ParticlizeController", "Particles generated, starting animation")

            state.value = State.RUNNING
            startAnimationLoop(params.effect)

            // Clear pending params
            pendingStartParams = null
        }
    }

    /**
     * Starts the particle effect
     */
    fun start(
        context: Context,
        effect: ParticleEffect = ParticleEffect.DISINTEGRATION,
        config: ParticleEffectConfig = effectConfig,
        onComplete: () -> Unit = {}
    ) {
        // Store parameters for delayed start
        effectConfig = config

        // Store parameters in case we need to retry
        pendingStartParams = StartParams(
            context,
            effect,
            config,
            onComplete
        )
        // Only start if not already running
        if (state.value != State.NONE) {
            return
        }

        // Transition to capturing state
        state.value = State.CAPTURING

        // Signal that we need a redraw to trigger content capture
        needsRedraw.value = !needsRedraw.value
    }

    /**
     * Start the animation loop
     */
    private fun startAnimationLoop(effect: ParticleEffect) {
        Log.d("ParticlizeController", "Starting animation loop")

        animationJob?.cancel()
        frameCount = 0

        animationJob = coroutineScope.launch(Dispatchers.Default) {
            val frameDelay = 16L // approx 60fps

            while (isActive) {
                val currentTime = System.currentTimeMillis()
                frameCount++

                // Update particle system
                val isAnimating = particleSystem?.updateAnimation(currentTime) ?: false

                if (!isAnimating) {
                    Log.d("ParticlizeController", "Animation completed after $frameCount frames")
                    withContext(Dispatchers.Main) {
                        destroy()
                        onEffectComplete?.invoke()
                    }
                    break
                }

                // Signal that we need to redraw
                withContext(Dispatchers.Main) {
                    needsRedraw.value = !needsRedraw.value
                }

                // Maintain frame rate
                delay(frameDelay)
            }
        }
    }

    /**
     * Render particles to canvas
     */
    fun render(canvas: Canvas, size: Size) {
        val particleData = particleSystem?.getParticleData() ?: return
        val effect = currentEffect ?: return

        val contentFade = particleSystem?.getCompletionPercentage() ?: 1f

        renderEngine.renderParticles(
            canvas = canvas,
            particleData = particleData,
            originalContent = contentBitmap,
            contentFadeAmount = contentFade,
            size = size
        )
    }

    // Other methods remain the same...

    /**
     * Checks if content capture is needed
     */
    fun needsCapture(): Boolean {
        return state.value == State.CAPTURING && pendingStartParams != null
    }

    /**
     * Checks if the effect has completed
     */
    fun hasEffectStarted(): Boolean = state.value == State.DESTROYED

    /**
     * Checks if the effect is currently running
     */
    fun isRunning(): Boolean = state.value == State.RUNNING

    /**
     * Get the current state
     */
    fun getState(): State {
        return state.value
    }

    /**
     * Get the redraw signal
     */
    fun getRedrawSignal(): State {
        needsRedraw.value
        return state.value
    }

    /**
     * Destroys the controller
     */
    private fun destroy() {
        animationJob?.cancel()
        animationJob = null

        state.value = State.DESTROYED
        particleSystem = null
        componentSize.value = null
        currentEffect = null
        onEffectComplete = null
        contentBitmap = null
        pendingStartParams = null
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        destroy()
        coroutineScope.cancel()
    }

    /**
     * States of the effect
     */
    enum class State {
        NONE,       // Initial state
        CAPTURING,  // Waiting for content capture
        RUNNING,    // Animation in progress
        DESTROYED   // Animation completed
    }
}