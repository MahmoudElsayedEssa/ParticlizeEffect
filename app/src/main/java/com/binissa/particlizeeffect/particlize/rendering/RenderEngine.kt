package com.binissa.particlizeeffect.particlize.rendering


import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import com.binissa.particlizeeffect.particlize.particle.SimpleParticleData

/**
 * Simple render engine focused on reliable particle visibility
 */
class RenderEngine {
    // Rendering options
    private var blendMode = BlendMode.SrcOver

    // Reused objects for efficiency
    private val paint = Paint()
    private val path = Path()

    /**
     * Render particles to canvas
     */
    fun renderParticles(
        canvas: Canvas,
        particleData: SimpleParticleData,
        originalContent: ImageBitmap?,
        contentFadeAmount: Float,
        size: Size
    ) {
        // Draw original content with fading if needed
        if (originalContent != null && contentFadeAmount < 1f) {
            paint.alpha = 1f - contentFadeAmount
            paint.blendMode = BlendMode.SrcOver

            canvas.drawImage(
                image = originalContent,
                topLeftOffset = Offset.Zero,
                paint = paint
            )
        }

        // Count active particles for logging
        var activeCount = 0

        // Draw each particle
        for (i in 0 until particleData.numParticles) {
            if (!particleData.active[i]) continue

            activeCount++

            // Get particle properties
            val x = particleData.posX[i]
            val y = particleData.posY[i]
            val color = particleData.colors[i]
            val size = particleData.sizes[i]
            val alpha = particleData.alphas[i]
            val scale = particleData.scales[i]
            val rotation = particleData.rotations[i]
            val shape = particleData.shapes[i]

            // Set up paint
            paint.color = color
            paint.alpha = alpha
            paint.blendMode = blendMode

            // Draw based on shape
            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(rotation)
            canvas.scale(scale, scale)

            when (shape) {
                0 -> { // Circle
                    canvas.drawCircle(Offset.Zero, size / 2, paint)
                }

                1 -> { // Square
                    val halfSize = size / 2
                    canvas.drawRect(
                        Rect(
                            -halfSize,
                            -halfSize,
                            halfSize,
                            halfSize
                        ),
                        paint
                    )
                }

                else -> { // Custom (triangle)
                    path.reset()
                    val halfSize = size / 2
                    path.moveTo(0f, -halfSize)
                    path.lineTo(halfSize, halfSize)
                    path.lineTo(-halfSize, halfSize)
                    path.close()
                    canvas.drawPath(path, paint)
                }
            }

            canvas.restore()
        }

        // Occasional logging of active particle count
        if (System.currentTimeMillis() % 300 < 20) {
            Log.d("SimpleRenderEngine", "Rendered $activeCount active particles")
        }
    }

    /**
     * Set the blend mode for particle rendering
     */
    fun setBlendMode(mode: BlendMode) {
        blendMode = mode
    }
}
//package com.binissa.particlizeeffect.particlize.rendering
//
//import android.util.Log
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.BlendMode
//import androidx.compose.ui.graphics.Canvas
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.graphics.Paint
//import androidx.compose.ui.graphics.Path
//import com.binissa.particlizeeffect.particlize.particle.SimpleParticleData
//
///**
// * Simple render engine focused on reliable particle visibility
// */
//class RenderEngine {
//    // Rendering options
//    private var blendMode = BlendMode.SrcOver
//
//    // Reused objects for efficiency
//    private val paint = Paint()
//    private val path = Path()
//
//    /**
//     * Render particles to canvas
//     */
//    fun renderParticles(
//        canvas: Canvas,
//        particleData: SimpleParticleData,
//        originalContent: ImageBitmap?,
//        contentFadeAmount: Float,
//        size: Size
//    ) {
//        // Draw original content with fading if needed
//        if (originalContent != null && contentFadeAmount < 1f) {
//            paint.alpha = 1f - contentFadeAmount
//            paint.blendMode = BlendMode.SrcOver
//
//            canvas.drawImage(
//                image = originalContent,
//                topLeftOffset = Offset.Zero,
//                paint = paint
//            )
//        }
//
//        // Count active particles for logging
//        var activeCount = 0
//
//        // Draw each particle
//        for (i in 0 until particleData.numParticles) {
//            if (!particleData.active[i]) continue
//
//            activeCount++
//
//            // Get particle properties
//            val x = particleData.posX[i]
//            val y = particleData.posY[i]
//            val color = particleData.colors[i]
//            val size = particleData.sizes[i]
//            val alpha = particleData.alphas[i]
//            val scale = particleData.scales[i]
//            val rotation = particleData.rotations[i]
//            val shape = particleData.shapes[i]
//
//            // Set up paint
//            paint.color = color
//            paint.alpha = alpha
//            paint.blendMode = blendMode
//
//            // Draw based on shape
//            canvas.save()
//            canvas.translate(x, y)
//            canvas.rotate(rotation)
//            canvas.scale(scale, scale)
//
//            when (shape) {
//                0 -> { // Circle
//                    canvas.drawCircle(Offset.Zero, size / 2, paint)
//                }
//
//                1 -> { // Square
//                    val halfSize = size / 2
//                    canvas.drawRect(
//                        Rect(
//                            -halfSize,
//                            -halfSize,
//                            halfSize,
//                            halfSize
//                        ),
//                        paint
//                    )
//                }
//
//                else -> { // Custom (triangle)
//                    path.reset()
//                    val halfSize = size / 2
//                    path.moveTo(0f, -halfSize)
//                    path.lineTo(halfSize, halfSize)
//                    path.lineTo(-halfSize, halfSize)
//                    path.close()
//                    canvas.drawPath(path, paint)
//                }
//            }
//
//            canvas.restore()
//        }
//
//        // Occasional logging of active particle count
//        if (System.currentTimeMillis() % 300 < 20) {
//            Log.d("SimpleRenderEngine", "Rendered $activeCount active particles")
//        }
//    }
//
//    /**
//     * Set the blend mode for particle rendering
//     */
//    fun setBlendMode(mode: BlendMode) {
//        blendMode = mode
//    }
//}