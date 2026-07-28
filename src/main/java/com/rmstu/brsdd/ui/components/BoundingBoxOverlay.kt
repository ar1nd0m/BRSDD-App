package com.rmstu.brsdd.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.rmstu.brsdd.detector.Detection
import com.rmstu.brsdd.ui.theme.colorForClass

/**
 * Draws detection boxes over a source image/frame of size [sourceWidth] x [sourceHeight],
 * scaling coordinates to whatever size this composable is actually laid out at.
 */
@Composable
fun BoundingBoxOverlay(
    detections: List<Detection>,
    sourceWidth: Int,
    sourceHeight: Int,
    modifier: Modifier = Modifier,
) {
    if (sourceWidth <= 0 || sourceHeight <= 0) return

    Box(modifier = modifier) {
        Canvas(modifier = Modifier) {
            val scaleX = size.width / sourceWidth
            val scaleY = size.height / sourceHeight

            detections.forEach { d ->
                val color = colorForClass(d.classId)
                val left = d.left * scaleX
                val top = d.top * scaleY
                val right = d.right * scaleX
                val bottom = d.bottom * scaleY

                drawRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = 5f),
                )

                val labelText = "${d.label} ${(d.confidence * 100).toInt()}%"
                val paint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.WHITE
                    textSize = 30f
                    isFakeBoldText = true
                }
                val bgPaint = android.graphics.Paint().apply {
                    this.color = with(color) {
                        android.graphics.Color.argb(
                            220, (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt()
                        )
                    }
                }
                val textWidth = paint.measureText(labelText)
                val labelTop = (top - 36f).coerceAtLeast(0f)
                drawContext.canvas.nativeCanvas.drawRect(
                    left, labelTop, left + textWidth + 16f, labelTop + 36f, bgPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    labelText, left + 8f, labelTop + 26f, paint
                )
            }
        }
    }
}
