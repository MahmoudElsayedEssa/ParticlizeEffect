package com.binissa.particlizeeffect.particlize.particle

import androidx.compose.ui.graphics.Color

data class SimpleParticleData(
    val numParticles: Int,
    val active: BooleanArray,
    val posX: FloatArray,
    val posY: FloatArray,
    val colors: Array<Color>,
    val sizes: FloatArray,
    val alphas: FloatArray,
    val scales: FloatArray,
    val rotations: FloatArray,
    val shapes: IntArray
)