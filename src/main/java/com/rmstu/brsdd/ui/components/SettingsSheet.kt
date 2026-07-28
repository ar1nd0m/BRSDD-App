package com.rmstu.brsdd.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    confidenceThreshold: Float,
    onConfidenceChange: (Float) -> Unit,
    iouThreshold: Float,
    onIouChange: (Float) -> Unit,
    gpuEnabled: Boolean,
    onGpuToggle: (Boolean) -> Unit,
    showOverlayStats: Boolean,
    onShowOverlayStatsToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Detection Settings", style = MaterialTheme.typography.titleMedium)

            SettingRow(
                label = "Confidence threshold",
                value = confidenceThreshold,
                onValueChange = onConfidenceChange,
            )
            SettingRow(
                label = "IoU (NMS) threshold",
                value = iouThreshold,
                onValueChange = onIouChange,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            ToggleRow(
                label = "GPU acceleration",
                checked = gpuEnabled,
                onCheckedChange = onGpuToggle,
            )
            ToggleRow(
                label = "Show FPS / latency overlay",
                checked = showOverlayStats,
                onCheckedChange = onShowOverlayStatsToggle,
            )

            Text(
                "Changing GPU acceleration reloads the model.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SettingRow(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "%.2f".format(value),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = 0.05f..0.95f)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
