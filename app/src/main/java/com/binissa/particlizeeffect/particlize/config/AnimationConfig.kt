package com.binissa.particlizeeffect.particlize.config

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode

/**
 * Configuration for animation timing and behavior
 */
data class AnimationConfig(
    val duration: Int = 1000,
    val easing: Easing = LinearEasing,
    val repeatMode: RepeatMode = RepeatMode.Restart,
    val repeatCount: Int = 0
) {
    companion object {
        val Default = AnimationConfig()
        
        // Preset configurations
        val Quick = AnimationConfig(
            duration = 500,
            easing = FastOutSlowInEasing
        )
        
        val Dramatic = AnimationConfig(
            duration = 1500,
            easing = LinearOutSlowInEasing
        )
    }
}