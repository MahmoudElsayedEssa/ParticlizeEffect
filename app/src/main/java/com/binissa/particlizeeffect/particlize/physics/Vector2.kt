package com.binissa.particlizeeffect.particlize.physics

import com.binissa.particlizeeffect.particlize.config.AssemblyConfig
import com.binissa.particlizeeffect.particlize.config.DisintegrationConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.particle.ParticleStorage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Vector2 utility class for physics calculations
 */
data class Vector2(val x: Float, val y: Float) {
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2 = Vector2(x * scalar, y * scalar)
    operator fun div(scalar: Float): Vector2 = Vector2(x / scalar, y / scalar)

    fun length(): Float = sqrt(x * x + y * y)
    fun normalized(): Vector2 {
        val len = length()
        return if (len > 0) this / len else Vector2(0f, 0f)
    }

    companion object {
        val ZERO = Vector2(0f, 0f)
    }
}