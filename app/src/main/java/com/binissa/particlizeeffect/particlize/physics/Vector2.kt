package com.binissa.particlizeeffect.particlize.physics

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 2D Vector class for particle physics
 */
data class Vector2(var x: Float, var y: Float) {
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2 = Vector2(x * scalar, y * scalar)
    operator fun div(scalar: Float): Vector2 = Vector2(x / scalar, y / scalar)
    operator fun plusAssign(other: Vector2) { x += other.x; y += other.y }
    operator fun minusAssign(other: Vector2) { x -= other.x; y -= other.y }
    operator fun timesAssign(scalar: Float) { x *= scalar; y *= scalar }
    operator fun divAssign(scalar: Float) { x /= scalar; y /= scalar }
    
    fun length(): Float = sqrt(x * x + y * y)
    
    fun normalized(): Vector2 {
        val len = length()
        return if (len > 0) Vector2(x / len, y / len) else Vector2(0f, 0f)
    }
    
    fun pow(power: Int): Float {
        return x.pow(power) + y.pow(power)
    }
    
    companion object {
        val ZERO = Vector2(0f, 0f)
        
        fun random(magnitude: Float = 1f): Vector2 {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            return Vector2(
                cos(angle) * magnitude,
                sin(angle) * magnitude
            )
        }
    }
}