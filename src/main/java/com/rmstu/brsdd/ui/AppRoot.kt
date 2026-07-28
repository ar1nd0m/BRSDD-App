package com.rmstu.brsdd.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.rmstu.brsdd.detector.RoadSignDetector

private enum class Tab(val label: String) { CAMERA("Live"), GALLERY("Photo"), ABOUT("About") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(Tab.CAMERA) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    // Detector is created once and reused across screens/tabs; released when the app is closed.
    var detector by remember { mutableStateOf<RoadSignDetector?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        try {
            detector = RoadSignDetector(context)
        } catch (t: Throwable) {
            loadError = t.message ?: "Failed to load the detection model."
        }
        onDispose { detector?.close() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BRSDD Road Sign Detector") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == Tab.CAMERA,
                    onClick = { currentTab = Tab.CAMERA },
                    icon = { Icon(Icons.Filled.CameraAlt, contentDescription = Tab.CAMERA.label) },
                    label = { Text(Tab.CAMERA.label) },
                )
                NavigationBarItem(
                    selected = currentTab == Tab.GALLERY,
                    onClick = { currentTab = Tab.GALLERY },
                    icon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = Tab.GALLERY.label) },
                    label = { Text(Tab.GALLERY.label) },
                )
                NavigationBarItem(
                    selected = currentTab == Tab.ABOUT,
                    onClick = { currentTab = Tab.ABOUT },
                    icon = { Icon(Icons.Filled.Info, contentDescription = Tab.ABOUT.label) },
                    label = { Text(Tab.ABOUT.label) },
                )
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                loadError != null -> {
                    Text(
                        "Couldn't load the detection model: $loadError",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                currentTab == Tab.CAMERA -> {
                    if (hasCameraPermission) {
                        detector?.let { CameraScreen(it) }
                    } else {
                        CameraPermissionRequest(onRequest = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        })
                    }
                }
                currentTab == Tab.GALLERY -> {
                    detector?.let { GalleryScreen(it) }
                }
                currentTab == Tab.ABOUT -> {
                    AboutScreen()
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionRequest(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(
            "Camera access is needed for live road sign detection.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        androidx.compose.material3.Button(
            onClick = onRequest,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Grant Camera Permission")
        }
    }
}
