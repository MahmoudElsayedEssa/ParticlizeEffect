// ParticlizeModifier.kt
package com.example.particlize.modifiers


import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import com.binissa.particlizeeffect.particlize.controller.ParticlizeController
import kotlinx.coroutines.launch

/**
 * Enables the particle effect for this composable.
 */
fun Modifier.particlize(
    controller: ParticlizeController,
    configureLayer: GraphicsLayer.() -> Unit = {},
): Modifier = composed {
    // If effect has completed, return an empty modifier
    if (controller.hasEffectStarted()) {
        return@composed Modifier
    }

    // Remember the graphics layer for recording content
    val graphicsLayer = rememberGraphicsLayer().apply {
        clip = true
        configureLayer.invoke(this)
    }
    val scope = rememberCoroutineScope()

    // Subscribe to redraws
    val state = controller.getRedrawSignal()

    // Clean up when disposed
    DisposableEffect(Unit) {
        onDispose {
            controller.cleanup()
        }
    }

    Modifier
        // Track size of the composable
        .onGloballyPositioned { coordinates ->
            controller.componentSize.value = Size(
                coordinates.size.width.toFloat(), coordinates.size.height.toFloat()
            )
        }
        // Draw content and particles
        .drawWithContent {
            when (val currentState = controller.getState()) {
                ParticlizeController.State.NONE -> {
                    // Just draw original content
                    drawContent()
                }

                ParticlizeController.State.CAPTURING -> {
                    // Draw the original content
                    drawContent()

                    // Capture content if needed
                    if (controller.needsCapture()) {
                        // First draw the content into graphics layer
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        try {
                            scope.launch {

                                // This is the critical part - capture content during the drawing phase
                                val bitmap = graphicsLayer.toImageBitmap()
                                if (bitmap.width > 0 && bitmap.height > 0) {
                                    // Only provide it to controller if valid
                                    controller.onContentCaptured(bitmap)
                                }
                            }
                        } catch (e: Exception) {
                            // Log error but don't crash
                            e.printStackTrace()
                        }
                    }
                }

                ParticlizeController.State.RUNNING -> {
                    // Don't draw the original content - only particles
                    drawIntoCanvas { canvas ->
                        controller.render(canvas, size)
                    }
                }

                ParticlizeController.State.DESTROYED -> {
                    // Draw nothing - component is gone
                }
            }
        }
}

/**
 * Create a new ParticlizeController that will be remembered across recompositions.
 */
@Composable
fun rememberParticlizeController(): ParticlizeController {
    return remember { ParticlizeController() }
}