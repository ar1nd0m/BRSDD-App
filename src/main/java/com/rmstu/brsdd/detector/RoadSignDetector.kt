package com.rmstu.brsdd.detector

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.rmstu.brsdd.util.ImageUtils
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Wraps the yolov8n.tflite model: input [1,640,640,3] float32 (RGB, 0..1),
 * output [1,33,8400] float32 = 4 box coords + 29 class scores, per anchor,
 * laid out channel-first (i.e. index as out[channel * 8400 + anchor]).
 */
class RoadSignDetector(
    context: Context,
    modelPath: String = "yolov8n.tflite",
    labelsPath: String = "labels.txt",
    useGpu: Boolean = true,
) {
    companion object {
        private const val TAG = "RoadSignDetector"
        const val INPUT_SIZE = 640
        const val NUM_CLASSES = 29
        const val NUM_CHANNELS = 4 + NUM_CLASSES // 33
        const val NUM_ANCHORS = 8400
    }

    private var interpreter: Interpreter
    private var gpuDelegate: GpuDelegate? = null
    val labels: List<String>
    val isUsingGpu: Boolean

    private val inputBuffer: ByteBuffer =
        ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }
    private val outputBuffer: ByteBuffer =
        ByteBuffer.allocateDirect(1 * NUM_CHANNELS * NUM_ANCHORS * 4).apply {
            order(ByteOrder.nativeOrder())
        }

    init {
        labels = context.assets.open(labelsPath).bufferedReader().readLines()
            .map { it.trim() }.filter { it.isNotEmpty() }

        val modelBuffer = loadModelFile(context, modelPath)

        var gpuEnabled = false
        var builtInterpreter: Interpreter
        val compatList = CompatibilityList()
        if (useGpu && compatList.isDelegateSupportedOnThisDevice) {
            try {
                val delegateOptions = compatList.bestOptionsForThisDevice
                val delegate = GpuDelegate(delegateOptions)
                gpuDelegate = delegate
                val options = Interpreter.Options().addDelegate(delegate)
                builtInterpreter = Interpreter(modelBuffer, options)
                gpuEnabled = true
            } catch (t: Throwable) {
                Log.w(TAG, "GPU delegate init failed, falling back to CPU", t)
                gpuDelegate?.close()
                gpuDelegate = null
                val cpuOptions = Interpreter.Options().setNumThreads(4)
                builtInterpreter = Interpreter(modelBuffer, cpuOptions)
            }
        } else {
            val cpuOptions = Interpreter.Options().setNumThreads(4)
            builtInterpreter = Interpreter(modelBuffer, cpuOptions)
        }
        interpreter = builtInterpreter
        isUsingGpu = gpuEnabled
    }

    private fun loadModelFile(context: Context, modelPath: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        FileInputStream(assetFileDescriptor.fileDescriptor).use { input ->
            val fileChannel = input.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    /**
     * Runs detection on [bitmap] and returns results already mapped back to
     * the ORIGINAL bitmap's pixel coordinates.
     */
    fun detect(
        bitmap: Bitmap,
        confidenceThreshold: Float = 0.25f,
        iouThreshold: Float = 0.45f,
    ): List<Detection> {
        val letterbox = ImageUtils.letterbox(bitmap, INPUT_SIZE)
        fillInputBuffer(letterbox.bitmap)

        outputBuffer.rewind()
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        val floatOut = outputBuffer.asFloatBuffer()
        val raw = FloatArray(NUM_CHANNELS * NUM_ANCHORS)
        floatOut.get(raw)

        val candidates = decode(raw, confidenceThreshold, letterbox)
        return NmsUtils.nms(candidates, iouThreshold)
    }

    private fun fillInputBuffer(letterboxed: Bitmap) {
        inputBuffer.rewind()
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        letterboxed.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255f
            val g = (pixel shr 8 and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        inputBuffer.rewind()
    }

    private fun decode(
        raw: FloatArray,
        confidenceThreshold: Float,
        letterbox: com.rmstu.brsdd.util.LetterboxResult,
    ): List<Detection> {
        val results = mutableListOf<Detection>()

        // Defensive check: the exported graph normally emits box coords already scaled
        // to the 640-pixel input space. If a different export ever emits 0..1 normalized
        // coords instead, detect it from the first anchor and rescale accordingly.
        val sampleW = raw[2 * NUM_ANCHORS + 0]
        val looksNormalized = sampleW in 0f..1.5f
        val coordScale = if (looksNormalized) INPUT_SIZE.toFloat() else 1f

        for (a in 0 until NUM_ANCHORS) {
            var bestClass = -1
            var bestScore = 0f
            for (c in 0 until NUM_CLASSES) {
                val score = raw[(4 + c) * NUM_ANCHORS + a]
                if (score > bestScore) {
                    bestScore = score
                    bestClass = c
                }
            }
            if (bestClass == -1 || bestScore < confidenceThreshold) continue

            val cx = raw[0 * NUM_ANCHORS + a] * coordScale
            val cy = raw[1 * NUM_ANCHORS + a] * coordScale
            val w = raw[2 * NUM_ANCHORS + a] * coordScale
            val h = raw[3 * NUM_ANCHORS + a] * coordScale

            val left = cx - w / 2f
            val top = cy - h / 2f
            val right = cx + w / 2f
            val bottom = cy + h / 2f

            val mapped = ImageUtils.mapBoxToOriginal(left, top, right, bottom, letterbox)
            val label = labels.getOrElse(bestClass) { "Class $bestClass" }

            results.add(
                Detection(
                    classId = bestClass,
                    label = label,
                    confidence = bestScore,
                    left = mapped[0], top = mapped[1], right = mapped[2], bottom = mapped[3],
                )
            )
        }
        return results
    }

    fun close() {
        interpreter.close()
        gpuDelegate?.close()
        gpuDelegate = null
    }
}
