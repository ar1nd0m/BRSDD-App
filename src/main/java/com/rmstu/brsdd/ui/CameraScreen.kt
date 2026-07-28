package com.rmstu.brsdd.ui

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.rmstu.brsdd.detector.Detection
import com.rmstu.brsdd.detector.RoadSignDetector
import com.rmstu.brsdd.ui.components.BoundingBoxOverlay
import com.rmstu.brsdd.ui.components.DetectionList
import com.rmstu.brsdd.ui.components.SettingsSheet
import com.rmstu.brsdd.util.FpsCounter
import com.rmstu.brsdd.util.ImageUtils
import java.util.concurrent.Executors

@Composable
fun CameraScreen(detector: RoadSignDetector) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var detections by remember { mutableStateOf<List<Detection>>(emptyList()) }
    var frameSize by remember { mutableStateOf(1 to 1) }
    var fps by remember { mutableStateOf(0f) }
    var latencyMs by remember { mutableStateOf(0L) }

    var confidence by remember { mutableStateOf(0.25f) }
    var iou by remember { mutableStateOf(0.45f) }
    var showStats by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val fpsCounter = remember { FpsCounter() }
    var isProcessing by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        if (isProcessing) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        isProcessing = true
                        try {
                            val bitmap: Bitmap = ImageUtils.imageProxyToBitmap(imageProxy)
                            val start = System.currentTimeMillis()
                            val result = detector.detect(bitmap, confidence, iou)
                            latencyMs = System.currentTimeMillis() - start
                            fps = fpsCounter.tick()
                            detections = result
                            frameSize = bitmap.width to bitmap.height
                        } catch (t: Throwable) {
                            // Keep the analyzer alive even if a single frame fails.
                        } finally {
                            isProcessing = false
                            imageProxy.close()
                        }
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
        )

        BoundingBoxOverlay(
            detections = detections,
            sourceWidth = frameSize.first,
            sourceHeight = frameSize.second,
            modifier = Modifier.fillMaxSize(),
        )

        if (showStats) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .wrapContentSize(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("FPS: %.1f".format(fps), style = MaterialTheme.typography.labelLarge)
                    Text("Latency: ${latencyMs} ms", style = MaterialTheme.typography.labelLarge)
                    Text(
                        if (detector.isUsingGpu) "Delegate: GPU" else "Delegate: CPU",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ) {
                DetectionList(
                    detections = detections,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                )
            }
        }

        FloatingActionButton(
            onClick = { showSettings = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }
    }

    if (showSettings) {
        SettingsSheet(
            confidenceThreshold = confidence,
            onConfidenceChange = { confidence = it },
            iouThreshold = iou,
            onIouChange = { iou = it },
            gpuEnabled = detector.isUsingGpu,
            onGpuToggle = { /* Requires reloading the interpreter; exposed for future use. */ },
            showOverlayStats = showStats,
            onShowOverlayStatsToggle = { showStats = it },
            onDismiss = { showSettings = false },
        )
    }
}
