package com.example.particlize.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.binissa.particlizeeffect.R
import com.binissa.particlizeeffect.particlize.config.AnimationConfig
import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.emission.EmissionPattern
import com.binissa.particlizeeffect.particlize.emission.EmissionType
import com.binissa.particlizeeffect.particlize.particle.ParticleEffect
import com.binissa.particlizeeffect.particlize.particle.ParticleShape
import com.example.particlize.modifiers.particlize
import com.example.particlize.modifiers.rememberParticlizeController


@Composable
fun ParticleEffectDemo() {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Controller for our effect
        val controller = rememberParticlizeController()

        // State to track if effect is active
        var isEffectActive by remember { mutableStateOf(false) }

        // Main content - a colorful image
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .padding(24.dp)
                .particlize(controller)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),

                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hamburger_svgrepo_com),
                    contentDescription = "",
                    modifier = Modifier.matchParentSize()
                )

            }
        }

        // Button to trigger effect
        Button(
            onClick = {
                if (!isEffectActive) {
                    isEffectActive = true
                    controller.start(
                        context = context,
                        effect = ParticleEffect.DISINTEGRATION,
                        particleConfig = ParticleConfig(
                            size = 10f,  // Larger particles for visibility
                            shape = ParticleShape.CIRCLE,
                            velocityMagnitude = 2.5f,  // Faster movement
                            gravity = 0.5f
                        ),
                        emissionConfig = EmissionConfig(
                            type = EmissionType.PATTERNED,
                            pattern = EmissionPattern.LEFT_TO_RIGHT,
                            particleCount = 10000
                        ),
                        animationConfig = AnimationConfig(
                            duration = 3000
                        ),
                        onComplete = {
                            isEffectActive = false
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Trigger Effect")
        }
    }
}