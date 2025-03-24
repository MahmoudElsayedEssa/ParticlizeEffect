package com.example.particlize.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.font.FontWeight
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
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf

@Composable
fun SimpleDensityDemo() {
    val context = LocalContext.current
    val controller = rememberParticlizeController()

    // State
    var isEffectActive by remember { mutableStateOf(false) }
    var effectType by remember { mutableStateOf(ParticleEffect.DISINTEGRATION) }
    var duration by remember { mutableStateOf(3000) }

    // Density controls
    var density by remember { mutableStateOf(0.5f) }
    var sizePriority by remember { mutableFloatStateOf(0.5f) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Content with particle effect
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .particlize(controller)
        ) {
            Image(
                painter = painterResource(id = R.drawable.hamburger_svgrepo_com),
                contentDescription = "Test Image",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Controls at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SIMPLE DENSITY CONTROL", fontWeight = FontWeight.Bold)

            // Effect type selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = effectType == ParticleEffect.DISINTEGRATION,
                        onClick = { effectType = ParticleEffect.DISINTEGRATION }
                    )
                    Text("Disintegration")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = effectType == ParticleEffect.ASSEMBLY,
                        onClick = { effectType = ParticleEffect.ASSEMBLY }
                    )
                    Text("Assembly")
                }
            }

            // Density slider
            Text("Particle Density: ${"%.2f".format(density)}")
            Slider(
                value = density,
                onValueChange = { density = it },
                valueRange = 0.1f..1.0f
            )

            // Size priority slider
            Text("Size Priority: ${"%.2f".format(sizePriority)}")
            Slider(
                value = sizePriority,
                onValueChange = { sizePriority = it },
                valueRange = 0f..1.0f
            )

            // Preset buttons for quick settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        density = 0.8f
                        sizePriority = 0.2f
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Many Small")
                }

                Button(
                    onClick = {
                        density = 0.5f
                        sizePriority = 0.5f
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Balanced")
                }

                Button(
                    onClick = {
                        density = 0.3f
                        sizePriority = 0.8f
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Few Large")
                }
            }

            // Duration slider
            Text("Duration: ${duration}ms")
            Slider(
                value = duration.toFloat(),
                onValueChange = { duration = it.toInt() },
                valueRange = 1000f..10000f
            )

            // Start button
            Button(
                onClick = {
                    if (!isEffectActive) {
                        isEffectActive = true

                        Log.d("DensityDemo", "Starting effect with density: $density, sizePriority: $sizePriority")

                        controller.start(
                            context = context,
                            effect = effectType,
                            particleConfig = ParticleConfig(
                                size = 5f,  // Base size before density calculations
                                shape = ParticleShape.CIRCLE
                            ),
                            emissionConfig = EmissionConfig(
                                type = EmissionType.INSTANT,
                                pattern = EmissionPattern.CENTER_OUT,
                                density = density,
                                sizePriority = sizePriority
                            ),
                            animationConfig = AnimationConfig(
                                duration = duration,
                                randomizeTimings = true,
                                randomizeAlpha = true,
                                randomizeScale = true
                            ),
                            onComplete = {
                                isEffectActive = false
                                Log.d("DensityDemo", "Effect completed")
                            }
                        )
                    }
                },
                enabled = !isEffectActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(if (isEffectActive) "Running..." else "Start Effect")
            }
        }
    }
}