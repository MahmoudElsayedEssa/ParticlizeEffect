package com.binissa.particlizeeffect.particlize.physics

import com.binissa.particlizeeffect.particlize.particle.ParticleStorage

interface PhysicsBehavior {
    fun updateParticle(
        storage: ParticleStorage,
        index: Int,
        deltaTime: Float,
        progress: Float
    )
}
