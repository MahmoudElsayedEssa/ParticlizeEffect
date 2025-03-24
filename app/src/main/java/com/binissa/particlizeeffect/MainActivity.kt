package com.binissa.particlizeeffect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.binissa.particlizeeffect.ui.theme.ParticlizeEffectTheme
import com.example.particlize.sample.ParticleEffectDemo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParticlizeEffectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ParticleEffectDemo()
                }
            }
        }
    }

}