package com.binissa.particlizeeffect.particlize.demos

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.binissa.particlizeeffect.particlize.config.DisintegrationConfig
import com.binissa.particlizeeffect.particlize.config.EmissionConfig
import com.binissa.particlizeeffect.particlize.config.ParticleConfig
import com.binissa.particlizeeffect.particlize.emission.EmissionPattern
import com.binissa.particlizeeffect.particlize.emission.EmissionType
import com.binissa.particlizeeffect.particlize.particle.ParticleEffect
import com.binissa.particlizeeffect.particlize.particle.ParticleShape
import com.example.particlize.modifiers.particlize
import com.example.particlize.modifiers.rememberParticlizeController
import android.util.Log

@Composable
fun AmazingEffectsDemo() {
    val context = LocalContext.current
    val controller = rememberParticlizeController()

    // State
    var isEffectActive by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf("Explosive") }
    var duration by remember { mutableStateOf(3000) }
    
    // Randomization settings
    var randomSizes by remember { mutableStateOf(true) }
    var randomAlphas by remember { mutableStateOf(true) }
    var randomRotations by remember { mutableStateOf(true) }
    
    // Dropdown menu state
    var showDropdown by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Content with particle effect
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .particlize(controller)
        ) {
            Text("Here's long text that will disapears")
//            Image(
//                painter = painterResource(id = R.drawable.hamburger_svgrepo_com),
//                contentDescription = "Test Image",
//                modifier = Modifier.fillMaxSize()
//            )
        }
        
        // Controls at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AMAZING DISINTEGRATION EFFECTS", fontWeight = FontWeight.Bold)
            
            // Preset selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Effect Type: ", fontWeight = FontWeight.Bold)
                
                // Current selection
                Button(
                    onClick = { showDropdown = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(selectedPreset)
                }
                
                // Dropdown menu
                Box {
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Explosive") },
                            onClick = { 
                                selectedPreset = "Explosive"
                                showDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Vortex") },
                            onClick = { 
                                selectedPreset = "Vortex"
                                showDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tornado") },
                            onClick = { 
                                selectedPreset = "Tornado"
                                showDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Black Hole") },
                            onClick = { 
                                selectedPreset = "BlackHole"
                                showDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Duration slider
            Text("Duration: ${duration}ms")
            Slider(
                value = duration.toFloat(),
                onValueChange = { duration = it.toInt() },
                valueRange = 1000f..10000f
            )
            
            // Randomization toggles
            Text("Particle Randomization:", fontWeight = FontWeight.Bold)
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = randomSizes,
                        onCheckedChange = { randomSizes = it }
                    )
                    Text("Random Sizes")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = randomAlphas,
                        onCheckedChange = { randomAlphas = it }
                    )
                    Text("Random Alphas")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = randomRotations,
                        onCheckedChange = { randomRotations = it }
                    )
                    Text("Random Rotations")
                }
            }
            
            // Start button
            Button(
                onClick = {
                    if (!isEffectActive) {
                        isEffectActive = true
                        
                        Log.d("AmazingDemo", "Starting effect: $selectedPreset")
                        
                        // Get disintegration config based on selected preset
                        val disConfig = DisintegrationConfig(
                            gravity = 0f,
                            windStrength = 1f,
                            windDirection = -1f,
                            windTurbulence = 5f,

                            turbulenceStrength = 1f,
                            friction = 0f,
                            windEnabled = true
                        )
                        
                        // Create particle config with selected disintegration config
                        val particleConfig = ParticleConfig(
                            size = 5f,
                            shape = ParticleShape.CIRCLE,
                            disintegration = disConfig
                        )
                        
                        // Create emission config with randomization settings
                        val emissionConfig = EmissionConfig(
                            type = EmissionType.PATTERNED,
                            pattern = EmissionPattern.LEFT_TO_RIGHT,
                            particleCount = 6000,
                            density = 0.1f,
                            sizePriority = 10f,
                            randomSizes = randomSizes,
                            randomAlphas = randomAlphas,
                            randomRotations = randomRotations
                        )
                        
                        controller.start(
                            context = context,
                            effect = ParticleEffect.DISINTEGRATION,
                            particleConfig = particleConfig,
                            emissionConfig = emissionConfig,
                            animationConfig = AnimationConfig(
                                duration = duration,
                                randomizeTimings = true
                            ),
                            onComplete = {
                                isEffectActive = false
                                Log.d("AmazingDemo", "Effect completed")
                            }
                        )
                    }
                },
                enabled = !isEffectActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(if (isEffectActive) "Running..." else "Start Amazing Effect!")
            }
        }
    }
}