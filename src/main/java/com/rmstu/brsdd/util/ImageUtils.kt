package com.rmstu.brsdd.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/** Result of letterboxing a bitmap into a square, model-ready input. */
data class LetterboxResult(
    val bitmap: Bitmap,
    val scale: Float,
    val padX: Float,
    val padY: Float,
    val originalWidth: Int,
    val originalHeight: Int,
)

object ImageUtils {

    /**
     * Resizes [src] to fit inside [targetSize] x [targetSize] while preserving aspect ratio,
     * padding the remainder with neutral grey (114,114,114) — matching Ultralytics' own
     * letterbox preprocessing so the trained model sees the same kind of input it was trained on.
     */
    fun letterbox(src: Bitmap, targetSize: Int = 640): LetterboxResult {
        val srcW = src.width
        val srcH = src.height
        val scale = min(
            targetSize.toFloat() / srcW,
            targetSize.toFloat() / srcH
        )
        val newW = (srcW * scale).toInt().coerceAtLeast(1)
        val newH = (srcH * scale).toInt().coerceAtLeast(1)

        val padX = (targetSize - newW) / 2f
        val padY = (targetSize - newH) / 2f

        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(android.graphics.Color.rgb(114, 114, 114))

        val resized = Bitmap.createScaledBitmap(src, newW, newH, true)
        canvas.drawBitmap(resized, padX, padY, Paint(Paint.FILTER_BITMAP_FLAG))

        return LetterboxResult(output, scale, padX, padY, srcW, srcH)
    }

    private fun min(a: Float, b: Float) = if (a < b) a else b

    /** Maps a box from 640x640 letterboxed model space back to the original image's pixel space. */
    fun mapBoxToOriginal(
        left: Float, top: Float, right: Float, bottom: Float,
        letterbox: LetterboxResult,
    ): FloatArray {
        val l = ((left - letterbox.padX) / letterbox.scale)
            .coerceIn(0f, letterbox.originalWidth.toFloat())
        val t = ((top - letterbox.padY) / letterbox.scale)
            .coerceIn(0f, letterbox.originalHeight.toFloat())
        val r = ((right - letterbox.padX) / letterbox.scale)
            .coerceIn(0f, letterbox.originalWidth.toFloat())
        val b = ((bottom - letterbox.padY) / letterbox.scale)
            .coerceIn(0f, letterbox.originalHeight.toFloat())
        return floatArrayOf(l, t, r, b)
    }

    /**
     * Converts a CameraX [ImageProxy] (YUV_420_888) into an upright ARGB [Bitmap],
     * applying the sensor rotation reported by CameraX so boxes line up with what the user sees.
     */
    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val yuvBytes = yuv420ToNv21(image)
        val yuvImage = YuvImage(yuvBytes, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val jpegBytes = out.toByteArray()
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

        val rotationDegrees = image.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    private fun yuv420ToNv21(image: ImageProxy): ByteArray {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yPlane.buffer.get(nv21, 0, ySize)

        val vBuffer = vPlane.buffer
        val uBuffer = uPlane.buffer
        val vRowStride = vPlane.rowStride
        val vPixelStride = vPlane.pixelStride

        // Interleave V and U into the NV21 layout (VUVUVU...) respecting stride.
        var offset = ySize
        val chromaHeight = image.height / 2
        val chromaWidth = image.width / 2
        for (row in 0 until chromaHeight) {
            for (col in 0 until chromaWidth) {
                val vIndex = row * vRowStride + col * vPixelStride
                val uIndex = row * uPlane.rowStride + col * uPlane.pixelStride
                if (offset + 1 < nv21.size && vIndex < vBuffer.limit() && uIndex < uBuffer.limit()) {
                    nv21[offset++] = vBuffer.get(vIndex)
                    nv21[offset++] = uBuffer.get(uIndex)
                }
            }
        }
        return nv21
    }
}
