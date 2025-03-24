package com.example.particlize.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import com.binissa.particlizeeffect.particlize.config.AnimationConfig
import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
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


    // Coroutine scope and job
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var animationJob: Job? = null

    private val renderEngine = RenderEngine()

    // Effect parameters
    private var currentEffect: ParticleEffect? = null

    // Callbacks
    private var onEffectComplete: (() -> Unit)? = null

    // Captured content
    private var contentBitmap: ImageBitmap? = null

    // Parameters for pending start
    private var pendingStartParams: StartParams? = null
    private var frameCount = 0

    private var particleSystem: ParticleSystem? = null

    /**
     * Data class to hold start parameters
     */
    private data class StartParams(
        val context: Context,
        val effect: ParticleEffect,
        val particleConfig: ParticleConfig,
        val emissionConfig: EmissionConfig,
        val animationConfig: AnimationConfig,
        val onComplete: () -> Unit
    )

    /**
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
        particleConfig: ParticleConfig ,
        emissionConfig: EmissionConfig = EmissionConfig.Default,
        animationConfig: AnimationConfig = AnimationConfig.Default,
        onComplete: () -> Unit = {}
    ) {
        // Store parameters for delayed start
        pendingStartParams = StartParams(
            context, effect, particleConfig, emissionConfig,
            animationConfig, onComplete
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

    // In ParticlizeController - change the render method
    fun render(canvas: Canvas, size: Size) {
        // Capture these values to avoid thread sync issues
        val particleData = particleSystem?.getParticleData() ?: return
        val effect = currentEffect ?: return
        val contentFade = particleSystem?.getCompletionPercentage() ?: 1f
        val content = contentBitmap

        // All rendering work happens in drawIntoCanvas, with no other work on main thread
        renderEngine.renderParticles(
            canvas = canvas,
            particleData = particleData,
            originalContent = content,
            contentFadeAmount = contentFade,
            size = size
        )
    }


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