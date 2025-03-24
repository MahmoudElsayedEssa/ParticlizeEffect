package com.binissa.particlizeeffect.particlize.particle

import androidx.compose.ui.graphics.Color
import java.util.Arrays

class ParticleStorage(val capacity: Int) {
    // Core particle properties
    val positionX = FloatArray(capacity)
    val positionY = FloatArray(capacity)
    val originalX = FloatArray(capacity)
    val originalY = FloatArray(capacity)
    val velocityX = FloatArray(capacity)
    val velocityY = FloatArray(capacity)
    
    // Visual properties
    val colors = Array(capacity) { Color.White }
    val sizes = FloatArray(capacity)
    val alphas = FloatArray(capacity) { 1f }
    val scales = FloatArray(capacity) { 1f }
    val rotations = FloatArray(capacity)
    val shapes = IntArray(capacity)
    
    // State tracking
    val active = BooleanArray(capacity) { false }
    val pending = BooleanArray(capacity) { false }
    val delays = FloatArray(capacity)
    
    // Count tracking
    var particleCount = 0
    
    fun reset() {
        particleCount = 0
        Arrays.fill(active, false)
        Arrays.fill(pending, false)
    }
    
    fun createParticle(
        x: Float, y: Float, 
        color: Color, 
        shape: Int, 
        size: Float
    ): Int {
        if (particleCount >= capacity) return -1
        
        val index = particleCount++
        
        // Set initial properties
        originalX[index] = x
        originalY[index] = y
        positionX[index] = x
        positionY[index] = y
        velocityX[index] = 0f
        velocityY[index] = 0f
        
        colors[index] = color
        sizes[index] = size
        shapes[index] = shape
        
        alphas[index] = 1f
        scales[index] = 1f
        rotations[index] = 0f
        
        return index
    }
    
    fun getActiveParticleIndices(): IntArray {
        return IntArray(particleCount) { i -> i }.filter { active[it] }.toIntArray()
    }
    
    fun getPendingParticleIndices(): IntArray {
        return IntArray(particleCount) { i -> i }.filter { pending[it] }.toIntArray()
    }
    
    fun activateParticle(index: Int) {
        if (index in 0..<particleCount) {
            pending[index] = false
            active[index] = true
        }
    }
    
    fun deactivateParticle(index: Int) {
        if (index in 0..<particleCount) {
            active[index] = false
            pending[index] = false
        }
    }
    
    fun getActiveCount(): Int = active.count { it }
    
    fun getPendingCount(): Int = pending.count { it }
}